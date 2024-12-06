package com.example.bookchigibakchigi.ui.searchbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.repository.AladinBookSearchRepository
import com.example.bookchigibakchigi.repository.BookSearchRepository

class SearchBookActivityViewModel(private val repository: AladinBookSearchRepository) {
    private val _aladinBookSearchResults = MutableLiveData<List<AladinBookItem>>()
    val aladinBookSearchResults: LiveData<List<AladinBookItem>> get() = _aladinBookSearchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


}