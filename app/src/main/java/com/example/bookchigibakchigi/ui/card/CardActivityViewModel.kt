package com.example.bookchigibakchigi.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.entity.BookEntity

class CardActivityViewModel : ViewModel() {

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    private val _bookInfo = MutableLiveData<String>("") // 기본값 설정
    private val _bookContent = MutableLiveData<String>("")

    fun setBookInfo(title: String) {
        _bookInfo.value = title
    }

    fun getBookInfoText(): String {
        return "${_bookInfo.value ?: ""} 中에서.."
    }

    fun setCurrentBook(book: BookEntity) {
        _currentBook.value = book
    }

    fun setBookContent(content: String) {
        _bookContent.value = content
    }

    fun getBookContentText(): String {
        return _bookContent.value ?: ""
    }

}