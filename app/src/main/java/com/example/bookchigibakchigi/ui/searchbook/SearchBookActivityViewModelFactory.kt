package com.example.bookchigibakchigi.ui.searchbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchigibakchigi.data.repository.AladinBookRepository

class SearchBookActivityViewModelFactory (
private val repository: AladinBookRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchBookActivityViewModel::class.java)) {
            return SearchBookActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}