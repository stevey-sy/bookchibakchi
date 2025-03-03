package com.example.bookchigibakchigi.ui.shared.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookShelfViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository
) : ViewModel() {

    // ✅ LiveData 그대로 사용 (UI에서 자동 업데이트됨)
//    val bookShelfItems: LiveData<List<BookEntity>> = bookShelfRepository.getShelfItems()

    private val _bookShelfItems = MutableLiveData<List<BookEntity>>()
    val bookShelfItems: LiveData<List<BookEntity>> get() = _bookShelfItems

    init {
        loadBooks() // 초기 데이터 로드
    }

    fun loadBooks() {
        viewModelScope.launch {
            bookShelfRepository.getShelfItems().observeForever { books ->
                _bookShelfItems.value = books
            }
        }
    }

    fun refreshShelf() {
        loadBooks() // 강제 새로고침
    }
}