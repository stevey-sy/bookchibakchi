package com.example.bookchigibakchigi.ui.mylibrary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.data.BookShelfItem
import com.example.bookchigibakchigi.repository.BookShelfRepository

class MyLibraryViewModel(private val repository: BookShelfRepository = BookShelfRepository()) : ViewModel() {
    private val _bookShelfItems = MutableLiveData<List<BookShelfItem>>()
    val bookShelfItems: MutableLiveData<List<BookShelfItem>> = _bookShelfItems

    init {
        loadShelfItems()
    }

    private fun loadShelfItems() {
        _bookShelfItems.value = repository.getShelfItems()
    }
}