package com.nuc.omeletteinputmethod.data.local

import com.nuc.omeletteinputmethod.data.model.UserDictionary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FakeUserDictionaryDao : UserDictionaryDao {
    private val store = mutableMapOf<Long, UserDictionary>()
    private var nextId: Long = 1

    override fun getAllWords() = flowOf(store.values.toList())

    override suspend fun insertWord(word: UserDictionary) {
        val entity = word.copy(id = nextId++)
        store[entity.id] = entity
    }

    override suspend fun getWord(word: String): UserDictionary? {
        return store.values.find { it.word == word }
    }

    override suspend fun updateFrequency(
        id: Long,
        timestamp: Long,
    ) {
        store[id]?.let {
            store[id] = it.copy(frequency = it.frequency + 1, lastUsedTimestamp = timestamp)
        }
    }

    override suspend fun getFrequencies(words: List<String>): List<UserDictionary> {
        return store.values.filter { it.word in words }
    }

    override fun getHighFrequencyWords(threshold: Int) =
        flowOf(
            store.values.filter { it.frequency >= threshold }.toList(),
        )
}

class UserDictionaryDaoTest {
    @Test
    fun `insertWord stores entity and assigns auto-increment id`() =
        runTest {
            val dao = FakeUserDictionaryDao()
            dao.insertWord(UserDictionary(word = "测试", frequency = 1))
            val all = dao.getAllWords().first()
            assertEquals(1, all.size)
            assertEquals("测试", all[0].word)
        }

    @Test
    fun `getWord returns matching word`() =
        runTest {
            val dao = FakeUserDictionaryDao()
            dao.insertWord(UserDictionary(word = "你好", frequency = 3))
            val result = dao.getWord("你好")
            assertNotNull(result)
            assertEquals("你好", result!!.word)
        }

    @Test
    fun `getWord returns null for non-existent word`() =
        runTest {
            val dao = FakeUserDictionaryDao()
            dao.insertWord(UserDictionary(word = "存在", frequency = 1))
            val result = dao.getWord("不存在")
            assertNull(result)
        }

    @Test
    fun `updateFrequency increments frequency`() =
        runTest {
            val dao = FakeUserDictionaryDao()
            dao.insertWord(UserDictionary(word = "高频", frequency = 5))
            val inserted = dao.getWord("高频")!!
            dao.updateFrequency(inserted.id, System.currentTimeMillis())
            val updated = dao.getWord("高频")
            assertNotNull(updated)
            assertEquals(6, updated!!.frequency)
        }

    @Test
    fun `getFrequencies filters by word list`() =
        runTest {
            val dao = FakeUserDictionaryDao()
            dao.insertWord(UserDictionary(word = "苹果", frequency = 10))
            dao.insertWord(UserDictionary(word = "香蕉", frequency = 3))
            dao.insertWord(UserDictionary(word = "橘子", frequency = 7))
            val results = dao.getFrequencies(listOf("苹果", "橘子"))
            assertEquals(2, results.size)
            assert(results.any { it.word == "苹果" })
            assert(results.any { it.word == "橘子" })
        }

    @Test
    fun `getHighFrequencyWords returns words above threshold`() =
        runTest {
            val dao = FakeUserDictionaryDao()
            dao.insertWord(UserDictionary(word = "常用", frequency = 100))
            dao.insertWord(UserDictionary(word = "偶尔", frequency = 3))
            dao.insertWord(UserDictionary(word = "中等", frequency = 20))
            val results = dao.getHighFrequencyWords(10).first()
            assertEquals(2, results.size)
            assert(results.any { it.word == "常用" })
            assert(results.any { it.word == "中等" })
        }
}
