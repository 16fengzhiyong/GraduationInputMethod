package com.nuc.omeletteinputmethod.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nuc.omeletteinputmethod.data.local.CellDictManager
import com.nuc.omeletteinputmethod.data.local.DictDeployer
import com.nuc.omeletteinputmethod.data.model.UserBigram
import com.nuc.omeletteinputmethod.data.model.UserDictionary
import com.nuc.omeletteinputmethod.data.model.UserBigramDao
import com.nuc.omeletteinputmethod.inputC.DictEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CandidateItem(val w: String, val f: Int, val s: Int = 0)

@Singleton
class DictionaryRepository
    @Inject
    constructor(
        private val dictEngine: DictEngine,
        private val userDictionaryDao: com.nuc.omeletteinputmethod.data.local.UserDictionaryDao,
        private val dictDeployer: DictDeployer,
        private val cellDictManager: CellDictManager,
        private val userBigramDao: UserBigramDao,
    ) {
        private val gson = Gson()
        private val candidateListType = object : TypeToken<List<CandidateItem>>() {}.type

        var lastCommittedWord: String = ""

        suspend fun initializeDictionary(): Boolean =
            withContext(Dispatchers.IO) {
                val deployed = dictDeployer.deployIfNeeded()
                if (!deployed) {
                    android.util.Log.e("DictionaryRepo", "Dict deployment failed")
                    return@withContext false
                }
                val ok = dictEngine.initialize(dictDeployer.filesDirPath)
                android.util.Log.i("DictionaryRepo", "Engine init: $ok")
                ok
            }

        suspend fun getInitialCandidates(pinyin: String): List<String> =
            withContext(Dispatchers.IO) {
                val enabledCats = cellDictManager.getEnabledCategories().firstOrNull().orEmpty()
                val activeCats = enabledCats.filter { cellDictManager.isCategoryEnabled(it) }

                val json =
                    if (activeCats.isEmpty()) {
                        dictEngine.search(pinyin)
                    } else {
                        dictEngine.searchWithCategories(pinyin, activeCats.toTypedArray())
                    }
                val items = parseCandidates(json)
                val candidates = items.map { it.w }

                if (activeCats.isNotEmpty()) {
                    val fallbackCandidates = mutableListOf<String>()
                    for (cat in activeCats) {
                        fallbackCandidates.addAll(cellDictManager.searchCategoryWords(cat, pinyin))
                    }
                    val merged = (candidates + fallbackCandidates).distinct()
                    sortCandidatesByUserFrequency(merged)
                } else {
                    sortCandidatesByUserFrequency(candidates)
                }
            }

        suspend fun getCandidatesWithBigram(
            pinyin: String,
            prevWord: String,
        ): List<String> =
            withContext(Dispatchers.IO) {
                val json = dictEngine.searchWithBigram(pinyin, prevWord)
                val items = parseCandidates(json)
                val candidates = items.map { it.w }
                sortCandidatesByUserFrequency(candidates)
            }

        suspend fun getCandidatesWithCategories(
            pinyin: String,
            enabledCategories: List<String>,
        ): List<String> =
            withContext(Dispatchers.IO) {
                val json =
                    if (enabledCategories.isEmpty()) {
                        dictEngine.search(pinyin)
                    } else {
                        dictEngine.searchWithCategories(pinyin, enabledCategories.toTypedArray())
                    }
                val items = parseCandidates(json)
                items.map { it.w }
            }

        suspend fun getAssociatedWords(chineseText: String): List<String> =
            withContext(Dispatchers.IO) {
                val json = dictEngine.associate(chineseText)
                val items = parseCandidates(json)
                items.map { it.w }
            }

        suspend fun recordSelection(word: String) =
            withContext(Dispatchers.IO) {
                val prev = lastCommittedWord
                lastCommittedWord = word

                val existing = userDictionaryDao.getWord(word)
                if (existing != null) {
                    userDictionaryDao.updateFrequency(existing.id, System.currentTimeMillis())
                } else {
                    userDictionaryDao.insertWord(
                        UserDictionary(
                            word = word,
                            frequency = 1,
                        ),
                    )
                }

                if (prev.isNotEmpty() && prev != word) {
                    val prevLastChar = if (prev.length > 1) prev.takeLast(1) else prev
                    val currFirstChar = if (word.length > 1) word.take(1) else word
                    val existingBigram = userBigramDao.getBigramFreq(prevLastChar, currFirstChar)
                    if (existingBigram != null) {
                        userBigramDao.upsertBigram(
                            UserBigram(
                                prevWord = prevLastChar,
                                nextWord = currFirstChar,
                                frequency = existingBigram + 1,
                                lastUsed = System.currentTimeMillis(),
                            ),
                        )
                    } else {
                        userBigramDao.upsertBigram(
                            UserBigram(
                                prevWord = prevLastChar,
                                nextWord = currFirstChar,
                                frequency = 1,
                            ),
                        )
                    }
                }
            }

        private fun parseCandidates(json: String): List<CandidateItem> {
            if (json.isEmpty() || json == "[]") return emptyList()
            return try {
                gson.fromJson(json, candidateListType)
            } catch (e: Exception) {
                emptyList()
            }
        }

        private suspend fun sortCandidatesByUserFrequency(candidates: List<String>): List<String> {
            if (candidates.isEmpty()) return emptyList()

            val userEntries = userDictionaryDao.getFrequencies(candidates)
            val userFreqMap = userEntries.associate { it.word to it }

            val now = System.currentTimeMillis()
            val halflife = 7.0 * 86400000.0
            val scorched =
                candidates.map { word ->
                    val entry = userFreqMap[word]
                    val userFreq = entry?.frequency ?: 0
                    val score =
                        if (userFreq > 0 && entry != null) {
                            val hoursSinceLastUse = (now - entry.lastUsedTimestamp).toDouble() / 3600000.0
                            val recentBoost = if (hoursSinceLastUse <= 1.0) 2.0 else 1.0
                            val daysSinceLastUse = (now - entry.lastUsedTimestamp).toDouble() / 86400000.0
                            val decay = Math.exp(-daysSinceLastUse / 7.0)
                            userFreq.toDouble() * decay * recentBoost
                        } else {
                            0.0
                        }
                    word to score
                }

            return scorched.sortedByDescending { it.second }.map { it.first }
        }
    }
