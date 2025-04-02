package com.example.bookchigibakchigi.ui.shared.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository
) : ViewModel() {
    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    fun setBook(book: BookEntity) {
        _currentBook.value = book
    }

    fun setCurrentBook(itemId: Int) {
//        bookShelfRepository.getBookById(itemId).observeForever { book ->
//            _currentBook.value = book
//        }
    }
}