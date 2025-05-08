package com.example.bookchigibakchigi.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.repository.AladinBookRepository
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.mapper.BookMapper
import com.example.bookchigibakchigi.model.SearchBookUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val repository: AladinBookRepository,
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddBookUiState>(AddBookUiState.Initial)
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    // DataBinding을 위한 데이터
    private val _bookItem = MutableStateFlow<SearchBookUiModel?>(null)
    val bookItem: StateFlow<SearchBookUiModel?> = _bookItem.asStateFlow()

    private val _coverUrl = MutableStateFlow<String?>(null)
    val coverUrl: StateFlow<String?> = _coverUrl.asStateFlow()

    private val _rating = MutableStateFlow(0f)
    val rating: StateFlow<Float> = _rating.asStateFlow()

    fun getBookItem(itemId: String, coverUrl: String) {
        viewModelScope.launch {
            try {
                val books = repository.getBookDetail(itemId)
                val searchBookUiModel = BookMapper.toSearchBookUiModel(books[0])
                _bookItem.value = searchBookUiModel
                _coverUrl.value = coverUrl
                _rating.value = searchBookUiModel.rate
                _uiState.value = AddBookUiState.Success(
                    book = searchBookUiModel,
                    coverUrl = coverUrl
                )
            } catch (e: Exception) {
                _uiState.value = AddBookUiState.Error(
                    message = "책 정보 저장 중 오류가 발생했습니다: ${e.message}",
                    retryAction = { getBookItem(itemId, coverUrl) }
                )
            }
        }
    }

    fun addBook() {
        viewModelScope.launch {
            val currentBookItem = _bookItem.value ?: return@launch
            
            val bookEntity = BookMapper.toEntityFromSearchBook(currentBookItem)

            if(bookEntity.title.isEmpty() || bookEntity.author.isEmpty() || bookEntity.publisher.isEmpty() || bookEntity.isbn.isEmpty()) {
                _uiState.value = AddBookUiState.Error(
                    message = "책 저장에 실패했습니다. 잠시 후에 다시 시도해주세요.",
                    retryAction = { addBook() }
                )
                return@launch
            }

            try {
                val bookDao = database.bookDao()
                val isExists = bookDao.isBookExists(bookEntity.isbn) > 0
                
                if (isExists) {
                    _uiState.value = AddBookUiState.Error(
                        message = "이미 저장된 책입니다.",
                        retryAction = { addBook() }
                    )
                    return@launch
                }

                val savedBook = bookDao.insertAndGetBook(bookEntity)
                _uiState.value = AddBookUiState.Success(
                    book = currentBookItem,
                    coverUrl = _coverUrl.value ?: "",
                    isSaved = true,
                    savedItemId = savedBook.itemId
                )
            } catch (e: Exception) {
                _uiState.value = AddBookUiState.Error(
                    message = "책 저장에 실패했습니다: ${e.message}",
                    retryAction = { addBook() }
                )
            }
        }
    }
}

sealed class AddBookUiState {
    data object Initial : AddBookUiState()  // 초기 상태
    data object Loading : AddBookUiState()  // 데이터 로딩 중
    // 성공 상태
    data class Success(
        val book: SearchBookUiModel,  // 선택된 책 정보
        val coverUrl: String,      // 책 커버 URL
        val isSaved: Boolean = false,  // 저장 완료 여부
        val savedItemId: Int? = null  // 저장된 책의 itemId
    ) : AddBookUiState()

    // 에러 상태
    data class Error(
        val message: String,  // 에러 메시지
        val retryAction: () -> Unit  // 재시도 액션
    ) : AddBookUiState()
}