package com.example.bookchigibakchigi.ui.record

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
@HiltViewModel
class RecordViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<RecordUiState>(RecordUiState.BeforeReading)
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _timerText = MutableStateFlow("00:00:00")
    val timerText: StateFlow<String> = _timerText.asStateFlow()

    private val _currentBook = MutableStateFlow<BookEntity?>(null)
    val currentBook: StateFlow<BookEntity?> = _currentBook.asStateFlow()

    var elapsedTime: Long = 0L
    private var timer: CountDownTimer? = null

    fun setCurrentBook(book: BookEntity) {
        _currentBook.value = book
    }

    fun getBookProgressText(): String {
        return _currentBook.value?.getProgressText() ?: "p. 0 / 0"
    }

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

    private fun pauseReading() {
        _uiState.value = RecordUiState.Paused
        pauseTimer()
    }

    private fun resumeReading() {
        _uiState.value = RecordUiState.Reading
        startTimer()
    }

    fun completeReading() {
        _uiState.value = RecordUiState.Completed
        pauseTimer()
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
