package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchigibakchigi.network.model.BookItem

class AddBookActivityViewModelFactory(
    private val book: BookItem?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBookActivityViewModel::class.java)) {
            return AddBookActivityViewModel().apply {
                setBookItem(book)
            } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}