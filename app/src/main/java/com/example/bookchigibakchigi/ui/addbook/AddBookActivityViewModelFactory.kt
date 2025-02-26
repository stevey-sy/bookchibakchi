package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.network.model.BookItem
import com.example.bookchigibakchigi.data.repository.AladinBookRepository

class AddBookActivityViewModelFactory(
    private val itemId: String,
    private val url: String,
    private val repository: AladinBookRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBookActivityViewModel::class.java)) {
            return AddBookActivityViewModel(repository).apply {
                getBookItem(itemId, url)
            } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}