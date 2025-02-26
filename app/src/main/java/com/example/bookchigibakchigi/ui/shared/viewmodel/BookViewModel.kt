package com.example.bookchigibakchigi.ui.shared.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.entity.BookEntity

class BookViewModel : ViewModel() {
    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    fun setBook(book: BookEntity) {
        _currentBook.value = book
    }

}