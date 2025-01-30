package com.example.bookchigibakchigi.ui.record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.entity.BookEntity

class RecordActivityViewModel : ViewModel() {

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _timerText = MutableLiveData<String>()
    val timerText: LiveData<String> get() = _timerText

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> get() = _status

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

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


}