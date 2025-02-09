package com.example.bookchigibakchigi.ui.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.entity.BookEntity

class CardActivityViewModel : ViewModel() {

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    val bookInfo = MutableLiveData<String>("") // 기본값 설정

    fun setBookInfo(title: String) {
        bookInfo.value = title
    }

    fun getBookInfoText(): String {
        return "${bookInfo.value ?: ""} 中에서.."
    }

    fun setCurrentBook(book: BookEntity) {
        _currentBook.value = book
    }
}