package com.example.bookchigibakchigi.ui.mylibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.BookShelfItem
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.repository.BookShelfRepository
import kotlinx.coroutines.launch

class MyLibraryViewModel(private val repository: BookShelfRepository) : ViewModel() {

    // 책 데이터를 저장할 LiveData
    private val _bookShelfItems = MutableLiveData<List<BookEntity>>()
    val bookShelfItems: LiveData<List<BookEntity>> = _bookShelfItems

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    init {
        loadShelfItems()
    }

    private fun loadShelfItems() {
        viewModelScope.launch {
            val items = repository.getShelfItems() // suspend 함수 호출
            _bookShelfItems.value = items

            // 첫 번째 책을 초기 선택값으로 설정
            if (items.isNotEmpty()) {
                _currentBook.value = items[0]
            }
        }
    }

    fun updateCurrentBook(position: Int) {
        _bookShelfItems.value?.let { bookList ->
            if (position in bookList.indices) {
                _currentBook.value = bookList[position]
            }
        }
    }
}