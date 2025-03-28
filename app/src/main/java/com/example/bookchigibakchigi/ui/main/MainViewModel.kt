package com.example.bookchigibakchigi.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel  @Inject constructor(
    private val bookShelfRepository: BookShelfRepository,
    private val photoCardRepository: PhotoCardRepository
) : ViewModel() {
    private val _bookShelfItems = MutableLiveData<List<BookEntity>>()
    val bookShelfItems: LiveData<List<BookEntity>> get() = _bookShelfItems

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    private val _photoCardList = MutableLiveData<List<PhotoCardWithTextContents>>()
    val photoCardList: LiveData<List<PhotoCardWithTextContents>> get() = _photoCardList


    init {
        loadBooks() // 초기 데이터 로드
    }

    fun loadBooks() {
        viewModelScope.launch {
            bookShelfRepository.getShelfItems().observeForever { books ->
                _bookShelfItems.value = books
            }
        }
    }

    fun refreshShelf() {
        loadBooks() // 강제 새로고침
    }

    fun setBook(book: BookEntity) {
        _currentBook.value = book
    }

    fun setCurrentBook(itemId: Int) {
        bookShelfRepository.getBookById(itemId).observeForever { book ->
            _currentBook.value = book
        }
    }

    /** 📌 특정 ISBN의 포토카드 리스트 가져오기 */
    fun loadPhotoCards(isbn: String) {
        viewModelScope.launch {
            val photoCards = photoCardRepository.getPhotoCardListByIsbn(isbn)
            _photoCardList.value = photoCards
        }
    }
}