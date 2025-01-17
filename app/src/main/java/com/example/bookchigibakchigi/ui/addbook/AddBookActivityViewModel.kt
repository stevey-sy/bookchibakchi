package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.network.model.BookItem
import com.example.bookchigibakchigi.repository.AladinBookRepository
import kotlinx.coroutines.launch

class AddBookActivityViewModel (
    private val repository: AladinBookRepository
) : ViewModel() {
    private val _bookItem = MutableLiveData<AladinBookItem?>()
    val bookItem: LiveData<AladinBookItem?> get() = _bookItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _coverUrl= MutableLiveData<String?>()
    val coverUrl: LiveData<String?> get() = _coverUrl

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun getBookItem(itemId: String, coverUrl: String) {
        _isLoading.value = true
        _coverUrl.value = coverUrl
        viewModelScope.launch {
            try {
                val book = repository.getBookDetail(itemId)
                _bookItem.value = book[0]
            } catch (e: Exception) {
                _errorMessage.value = "책 정보를 가져오는 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}