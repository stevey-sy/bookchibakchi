package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.network.model.BookItem

class AddBookActivityViewModel : ViewModel() {
    private val _bookItem = MutableLiveData<BookItem?>()
    val bookItem: LiveData<BookItem?> get() = _bookItem

    fun setBookItem(newBookItem: BookItem?) {
        _bookItem.value = newBookItem
    }
}