package com.example.bookchigibakchigi.ui.record

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.model.BookUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordUiState>(RecordUiState.BeforeReading)
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _timerText = MutableStateFlow("00:00:00")
    val timerText: StateFlow<String> = _timerText.asStateFlow()

    private val _selectedBookId = MutableStateFlow<Int?>(null)
    val selectedBookId: StateFlow<Int?> = _selectedBookId.asStateFlow()

    // 선택된 Todo에 대한 Flow (자동 갱신)
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedBook: StateFlow<BookUiModel?> = selectedBookId
        .filterNotNull()
        .flatMapLatest { id ->
            bookShelfRepository.getBookById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun setSelectedBook(id: Int) {
        _selectedBookId.value = id
    }

    var elapsedTime: Long = 0L
    private var timer: CountDownTimer? = null


    fun toggleTimer() {
        when (_uiState.value) {
            is RecordUiState.BeforeReading -> startReading()
            is RecordUiState.Reading -> pauseReading()
            is RecordUiState.Paused -> resumeReading()
            is RecordUiState.Completed -> { pauseReading() }
        }
    }

    private fun startReading() {
        _uiState.value = RecordUiState.Reading
        startTimer()
    }

    fun pauseReading() {
        _uiState.value = RecordUiState.Paused
        pauseTimer()
    }

    private fun resumeReading() {
        _uiState.value = RecordUiState.Reading
        startTimer()
    }

    suspend fun completeReading() {
        selectedBook.value?.let { book ->
            val elapsedTimeInSeconds = elapsedTime / 1000
            bookShelfRepository.updateReadingProgress(book.itemId, book.totalPageCnt, elapsedTimeInSeconds.toInt())
            _uiState.value = RecordUiState.Completed
        }
    }

    suspend fun updateReadingProgress(page: Int): Boolean {
        return selectedBook.value?.let { book ->
            val elapsedTimeInSeconds = elapsedTime / 1000
            bookShelfRepository.updateReadingProgress(book.itemId, page, elapsedTimeInSeconds.toInt())
        } ?: false
    }

    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                _timerText.value = formatTime(elapsedTime)
            }

            override fun onFinish() {}
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}

sealed interface RecordUiState {
    data object BeforeReading : RecordUiState
    data object Reading : RecordUiState
    data object Paused : RecordUiState
    data object Completed : RecordUiState
}
