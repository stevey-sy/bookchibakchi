package com.example.bookchigibakchigi.ui.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.entity.BookEntity

class MemoActivityViewModel  : ViewModel() {

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    fun setCurrentBook(book: BookEntity) {
        _currentBook.value = book
    }

    private val _page = MutableLiveData<String>()
    val page: LiveData<String> get() = _page

}