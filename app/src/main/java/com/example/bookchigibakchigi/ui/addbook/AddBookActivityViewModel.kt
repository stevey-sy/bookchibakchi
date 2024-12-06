package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.network.model.BookItem

class AddBookActivityViewModel : ViewModel() {
    private val _bookItem = MutableLiveData<AladinBookItem?>()
    val bookItem: LiveData<AladinBookItem?> get() = _bookItem

    fun setBookItem(newBookItem: AladinBookItem?) {
        _bookItem.value = newBookItem
    }
}