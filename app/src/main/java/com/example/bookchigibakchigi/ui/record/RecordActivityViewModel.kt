package com.example.bookchigibakchigi.ui.record

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity

class RecordActivityViewModel : ViewModel() {

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _timerText = MutableLiveData<String>()
    val timerText: LiveData<String> get() = _timerText

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> get() = _status

    private val _pauseButtonIcon = MutableLiveData<Int>(R.drawable.ic_pause_white) //
    val pauseButtonIcon: LiveData<Int> get() = _pauseButtonIcon

    private val _backgroundColor = MutableLiveData<Int>(R.color.black) // 초기 배경색: 흰색
    val backgroundColor: LiveData<Int> get() = _backgroundColor

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    private var elapsedTime: Long = 0L  // 총 경과 시간 (밀리초 단위)
    private var timer: CountDownTimer? = null  // 타이머 객체

    fun setCurrentBook(book: BookEntity) {
        _currentBook.value = book
    }


    fun getBookProgressText(): LiveData<String> {
        val progressText = MutableLiveData<String>()
        _currentBook.observeForever { book ->
            progressText.value = book?.getProgressText() ?: "p. 0 / 0"
        }
        return progressText
    }

    fun toggleTimer() {
        if (_isRecording.value == true) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isRecording.value = true
        _status.value = "읽고 있는 중..."
        _pauseButtonIcon.value = R.drawable.ic_pause_white
        _backgroundColor.value = R.color.black

        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) { // 무한 타이머 (1초마다 갱신)
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                _timerText.value = formatTime(elapsedTime)
            }

            override fun onFinish() {}
        }.start()
    }

    private fun pauseTimer() {
        _isRecording.value = false
        _status.value = "일시정지됨"
        _pauseButtonIcon.value = R.drawable.ic_play_button
        _backgroundColor.value = R.color.gray
        timer?.cancel()
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }



}