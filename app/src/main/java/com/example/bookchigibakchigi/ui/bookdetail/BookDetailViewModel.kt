package com.example.bookchigibakchigi.ui.bookdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.repository.BookShelfRepository
import kotlinx.coroutines.launch

class BookDetailViewModel(private val repository: BookShelfRepository): ViewModel() {
    // 책 데이터를 저장할 LiveData
    private val _bookShelfItems = MutableLiveData<List<BookEntity>>()
    val bookShelfItems: LiveData<List<BookEntity>> = _bookShelfItems

    init {
        loadShelfItems()
    }

    private fun loadShelfItems() {
        viewModelScope.launch {
            val items = repository.getShelfItems() // suspend 함수 호출
            _bookShelfItems.value = items
        }
    }
}