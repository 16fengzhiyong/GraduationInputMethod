package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.inputC.InputC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepository @Inject constructor(
    private val inputC: InputC,
    private val userDictionaryDao: com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
) {

    suspend fun recordSelection(word: String) = withContext(Dispatchers.IO) {
        val existing = userDictionaryDao.getWord(word)
        if (existing != null) {
            userDictionaryDao.updateFrequency(existing.id, System.currentTimeMillis())
        } else {
            userDictionaryDao.insertWord(
                com.nuc.omeletteinputmethod.data.model.UserDictionary(
                    word = word,
                    frequency = 1
                )
            )
        }
    }

    suspend fun initializeDictionary() = withContext(Dispatchers.IO) {
        inputC.stringFromJNI()
    }

    suspend fun getInitialCandidates(pinyin: String): List<String> = withContext(Dispatchers.IO) {
        val rawCandidates = parseJniResult(inputC.getStringOfFristFromJNI(pinyin))
        sortCandidatesByFrequency(rawCandidates)
    }

    private suspend fun sortCandidatesByFrequency(candidates: List<String>): List<String> {
        if (candidates.isEmpty()) return emptyList()
        // Optimization: Fetch top frequent words from DB that are in this candidate list?
        // Or simpler: Just get high frequency words and check overlap?
        // Given typically short lists (<50), we can check individually or load all frequent words.
        // For performance, let's just respect the ORDER returned by JNI, but bubble up any "very frequent" words?
        // Actually, the requirement is "habit recording".
        
        // Let's do a simple re-sort:
        // 1. Get all words in candidate list from DB to find their frequencies
        // 2. Sort descending by frequency
        
        // Ideally we would do `userDictionaryDao.getFrequencies(candidates)` but we only have `getWord`.
        // Let's iterate (might be N queries, acceptable for N<10 on IO thread).
        
        val candidatesWithFreq = candidates.map { word ->
            val entry = userDictionaryDao.getWord(word)
            word to (entry?.frequency ?: 0)
        }
        
        return@withContext candidatesWithFreq.sortedByDescending { it.second }.map { it.first }
    }

    suspend fun getSecondaryCandidates(pinyin: String, attempt: Int): List<String> = withContext(Dispatchers.IO) {
        val result = inputC.getStringOfScendFromJNI(pinyin, attempt)
        parseJniResult(result)
    }
    
    suspend fun getAssociatedWords(chineseChar: String): List<String> = withContext(Dispatchers.IO) {
        val result = inputC.getStringForReadyFromJNI(chineseChar)
        parseJniResult(result)
    }

    private fun parseJniResult(dniString: String?): List<String> {
        if (dniString.isNullOrEmpty()) return emptyList()
        return dniString.split(",").filter { it.isNotEmpty() }
    }
}
