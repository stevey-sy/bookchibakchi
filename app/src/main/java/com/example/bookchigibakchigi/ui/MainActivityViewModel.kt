package com.example.bookchigibakchigi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.repository.BookShelfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: BookShelfRepository) : ViewModel() {
    // 책 데이터를 저장할 LiveData
    // ✅ 책 데이터를 저장할 StateFlow
    private val _bookShelfItems = MutableStateFlow<List<BookEntity>>(emptyList())
    val bookShelfItems: StateFlow<List<BookEntity>> = _bookShelfItems.asStateFlow()

    private val _currentBook = MutableStateFlow<BookEntity?>(null)
    val currentBook: StateFlow<BookEntity?> = _currentBook.asStateFlow()

    // ✅ 현재 선택된 아이템의 위치를 저장할 변수 (초기값: -1)
    private val _currentPosition = MutableStateFlow(-1)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    init {
        loadShelfItems()
    }

    private fun loadShelfItems() {
        viewModelScope.launch {
            val items = repository.getShelfItems() // suspend 함수 호출
            _bookShelfItems.value = items
        }
    }

    fun updateCurrentBook(position: Int) {
        val bookList = _bookShelfItems.value
        if (position in bookList.indices) {
            _currentBook.value = bookList[position]
        }
    }

    fun reloadBooks() {
        loadShelfItems()
    }
}

