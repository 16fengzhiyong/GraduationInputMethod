package com.nuc.omeletteinputmethod.ui.keyboard

import android.content.Context
import android.content.SharedPreferences
import com.nuc.omeletteinputmethod.data.repository.DictionaryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: DictionaryRepository
    private lateinit var viewModel: KeyboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        repository = mockk()
        coEvery { repository.initializeDictionary() } returns true
        coEvery { repository.getInitialCandidates(any()) } returns emptyList()
        coEvery { repository.recordSelection(any()) } returns Unit
        coEvery { repository.lastCommittedWord = any() } returns Unit
        coEvery { repository.isWordPinned(any()) } returns false

        val mockContext = mockk<Context>()
        val mockPrefs = mockk<SharedPreferences>()
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.getBoolean(any(), any()) } returns true
        every { mockPrefs.getString(any(), any()) } returns "xiaohe"
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("zh↔z", "ch↔c", "sh↔s", "n↔l", "ang↔an", "eng↔en", "ing↔in")
        every { mockPrefs.getFloat(any(), any()) } returns 0.4f
        every { mockPrefs.edit() } returns mockk(relaxed = true)

        val mockInputStatDao = mockk<com.nuc.omeletteinputmethod.data.model.InputStatDao>(relaxed = true)
        val mockPreferenceManager = mockk<KeyboardPreferenceManager>(relaxed = true)

        viewModel = KeyboardViewModel(repository, mockContext, mockInputStatDao, mockPreferenceManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onKeyChar in ALPHA mode updates inputBuffer and fetches candidates`() =
        runTest {
            coEvery { repository.getInitialCandidates("a") } returns listOf("阿", "啊", "吖")
            viewModel.onKeyChar('a')
            advanceUntilIdle()
            assertEquals("a", viewModel.state.value.inputBuffer)
            assertEquals(listOf("阿", "啊", "吖"), viewModel.state.value.candidates)
        }

    @Test
    fun `onKeyChar in SYMBOL mode directly commits text`() =
        runTest {
            viewModel.setMode(KeyboardMode.SYMBOL)
            advanceUntilIdle()
            viewModel.onKeyChar('#')
            advanceUntilIdle()
            assertEquals("", viewModel.state.value.inputBuffer)
            assertEquals(emptyList<Any>(), viewModel.state.value.candidates)
        }

    @Test
    fun `onKeyChar in NUMBER mode directly commits text`() =
        runTest {
            viewModel.setMode(KeyboardMode.NUMBER)
            advanceUntilIdle()
            viewModel.onKeyChar('5')
            advanceUntilIdle()
            assertEquals("", viewModel.state.value.inputBuffer)
            assertEquals(emptyList<Any>(), viewModel.state.value.candidates)
        }

    @Test
    fun `onDelete removes last character when buffer not empty`() =
        runTest {
            coEvery { repository.getInitialCandidates("a") } returns listOf("阿")
            coEvery { repository.getInitialCandidates("") } returns emptyList()
            viewModel.onKeyChar('a')
            advanceUntilIdle()
            assertEquals("a", viewModel.state.value.inputBuffer)
            viewModel.onDelete()
            advanceUntilIdle()
            assertEquals("", viewModel.state.value.inputBuffer)
            assertEquals(emptyList<Any>(), viewModel.state.value.candidates)
        }

    @Test
    fun `onSpace commits first candidate when candidates available`() =
        runTest {
            coEvery { repository.getInitialCandidates("ni") } returns listOf("你", "尼", "泥")
            viewModel.onKeyChar('n')
            viewModel.onKeyChar('i')
            advanceUntilIdle()
            assertEquals("ni", viewModel.state.value.inputBuffer)
            viewModel.onSpace()
            advanceUntilIdle()
            assertEquals("", viewModel.state.value.inputBuffer)
            coVerify(exactly = 1) { repository.recordSelection("你") }
        }

    @Test
    fun `onSpace commits space when no candidates`() =
        runTest {
            viewModel.onSpace()
            advanceUntilIdle()
            assertEquals("", viewModel.state.value.inputBuffer)
            assertEquals(emptyList<Any>(), viewModel.state.value.candidates)
        }

    @Test
    fun `onCandidateSelected commits text and clears buffer`() =
        runTest {
            viewModel.onCandidateSelected("测试")
            advanceUntilIdle()
            assertEquals("", viewModel.state.value.inputBuffer)
            assertEquals(emptyList<Any>(), viewModel.state.value.candidates)
            coVerify(exactly = 1) { repository.recordSelection("测试") }
        }

    @Test
    fun `toggleShift toggles isShifted state`() =
        runTest {
            assertFalse(viewModel.state.value.isShifted)
            viewModel.toggleShift()
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isShifted)
            viewModel.toggleShift()
            advanceUntilIdle()
            assertFalse(viewModel.state.value.isShifted)
        }

    @Test
    fun `setMode changes keyboard mode`() =
        runTest {
            assertEquals(KeyboardMode.ALPHA, viewModel.state.value.mode)
            viewModel.setMode(KeyboardMode.SYMBOL)
            advanceUntilIdle()
            assertEquals(KeyboardMode.SYMBOL, viewModel.state.value.mode)
            viewModel.setMode(KeyboardMode.NUMBER)
            advanceUntilIdle()
            assertEquals(KeyboardMode.NUMBER, viewModel.state.value.mode)
        }
}