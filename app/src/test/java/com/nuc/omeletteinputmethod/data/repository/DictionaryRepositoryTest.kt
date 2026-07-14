package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.UserDictionaryDao
import com.nuc.omeletteinputmethod.data.model.UserDictionary
import com.nuc.omeletteinputmethod.inputC.DictEngine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DictionaryRepositoryTest {
    private lateinit var repository: DictionaryRepository
    private lateinit var dictEngine: DictEngine
    private lateinit var dao: UserDictionaryDao

    @Before
    fun setUp() {
        dictEngine = mockk()
        dao = mockk()
        coEvery { dictEngine.initialize(any()) } returns true
        coEvery { dictEngine.search(any()) } returns """[{"w":"好","f":100},{"w":"号","f":50}]"""
        coEvery { dictEngine.associate(any()) } returns """[{"w":"好人","f":80},{"w":"好吃","f":60}]"""
        coEvery { dao.getWord(any()) } returns null
        coEvery { dao.insertWord(any()) } returns Unit
        coEvery { dao.updateFrequency(any(), any()) } returns Unit
        coEvery { dao.getFrequencies(any()) } returns emptyList()
    }

    @Test
    fun `getInitialCandidates returns candidate list from search`() =
        runTest {
            val result = repository.getInitialCandidates("hao")
            assertEquals(2, result.size)
            assertEquals("好", result[0])
            assertEquals("号", result[1])
        }

    @Test
    fun `getInitialCandidates sorts by user frequency`() =
        runTest {
            coEvery { dictEngine.search("hao") } returns """[{"w":"好","f":100},{"w":"号","f":50},{"w":"耗","f":30}]"""
            coEvery {
                dao.getFrequencies(listOf("好", "号", "耗"))
            } returns
                listOf(
                    UserDictionary(
                        id = 1, word = "好", frequency = 10,
                        lastUsedTimestamp = System.currentTimeMillis(),
                    ),
                    UserDictionary(
                        id = 2, word = "号", frequency = 3,
                        lastUsedTimestamp = System.currentTimeMillis(),
                    ),
                    UserDictionary(
                        id = 3, word = "耗", frequency = 1,
                        lastUsedTimestamp = System.currentTimeMillis(),
                    ),
                )
            val result = repository.getInitialCandidates("hao")
            assertEquals(3, result.size)
            assertEquals("好", result[0])
        }

    @Test
    fun `getAssociatedWords returns word list from associate`() =
        runTest {
            val result = repository.getAssociatedWords("好")
            assertEquals(2, result.size)
            assertEquals("好人", result[0])
            assertEquals("好吃", result[1])
        }

    @Test
    fun `initializeDictionary deploys and initializes engine`() =
        runTest {
            val ok = repository.initializeDictionary()
            assertTrue(ok)
            coVerify(exactly = 1) { dictEngine.initialize(any()) }
        }

    @Test
    fun `recordSelection inserts new word when not in dictionary`() =
        runTest {
            coEvery { dao.getWord("新词") } returns null
            repository.recordSelection("新词")
            coVerify(exactly = 1) { dao.insertWord(any()) }
        }

    @Test
    fun `recordSelection updates frequency when word exists`() =
        runTest {
            coEvery { dao.getWord("旧词") } returns
                UserDictionary(
                    id = 42, word = "旧词", frequency = 5,
                )
            repository.recordSelection("旧词")
            coVerify(exactly = 1) { dao.updateFrequency(42, any()) }
        }

    @Test
    fun `empty search result returns empty list`() =
        runTest {
            coEvery { dictEngine.search("xxx") } returns "[]"
            val result = repository.getInitialCandidates("xxx")
            assertTrue(result.isEmpty())
        }
}
