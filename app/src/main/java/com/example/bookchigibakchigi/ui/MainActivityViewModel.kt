package com.example.bookchigibakchigi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.repository.BookShelfRepository
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: BookShelfRepository) : ViewModel() {
    // 책 데이터를 저장할 LiveData
    private val _bookShelfItems = MutableLiveData<List<BookEntity>>()
    val bookShelfItems: LiveData<List<BookEntity>> = _bookShelfItems

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook

    // 현재 선택된 아이템의 위치를 저장할 변수
    private val _currentPosition = MutableLiveData<Int>().apply { value = -1 } // 초기값: 선택 안 됨
    val currentPosition: LiveData<Int> = _currentPosition

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
        _bookShelfItems.value?.let { bookList ->
            if (position in bookList.indices) {
                _currentBook.value = bookList[position]
            }
        }
    }

    fun reloadBooks() {
        loadShelfItems()
    }
}

