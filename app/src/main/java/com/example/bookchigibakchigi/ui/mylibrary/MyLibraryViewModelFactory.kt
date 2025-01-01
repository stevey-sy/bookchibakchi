package com.example.bookchigibakchigi.ui.mylibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchigibakchigi.repository.BookShelfRepository

class MyLibraryViewModelFactory(private val repository: BookShelfRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyLibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyLibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}