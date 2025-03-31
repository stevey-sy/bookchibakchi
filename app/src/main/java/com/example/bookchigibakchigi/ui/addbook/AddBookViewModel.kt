package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.data.repository.AladinBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val repository: AladinBookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddBookUiState>(AddBookUiState.Initial)
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    // DataBinding을 위한 데이터
    private val _bookItem = MutableStateFlow<AladinBookItem?>(null)
    val bookItem: StateFlow<AladinBookItem?> = _bookItem.asStateFlow()

    private val _coverUrl = MutableStateFlow<String?>(null)
    val coverUrl: StateFlow<String?> = _coverUrl.asStateFlow()

    fun getBookItem(itemId: String, coverUrl: String) {
        viewModelScope.launch {
            _uiState.value = AddBookUiState.Loading
            try {
                val book = repository.getBookDetail(itemId)
                _bookItem.value = book[0]
                _coverUrl.value = coverUrl
                _uiState.value = AddBookUiState.Success(
                    book = book[0],
                    coverUrl = coverUrl
                )
            } catch (e: Exception) {
                _uiState.value = AddBookUiState.Error(
                    message = "책 정보를 가져오는 중 오류가 발생했습니다: ${e.message}",
                    retryAction = { getBookItem(itemId, coverUrl) }
                )
            }
        }
    }
}

sealed class AddBookUiState {
    data object Loading : AddBookUiState()  // 데이터 로딩 중
    data object Initial : AddBookUiState()  // 초기 상태
    
    // 성공 상태
    data class Success(
        val book: AladinBookItem,  // 선택된 책 정보
        val coverUrl: String,      // 책 커버 URL
        val isSaved: Boolean = false  // 저장 완료 여부
    ) : AddBookUiState()
    
    // 에러 상태
    data class Error(
        val message: String,  // 에러 메시지
        val retryAction: () -> Unit  // 재시도 액션
    ) : AddBookUiState()
}