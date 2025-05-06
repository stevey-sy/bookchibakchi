package com.example.bookchigibakchigi.ui.main

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.MemoRepository
import com.example.bookchigibakchigi.data.repository.TagRepository
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.model.MemoUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository,
    private val memoRepository: MemoRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainViewUiState>(MainViewUiState.Loading)
    val uiState: StateFlow<MainViewUiState> = _uiState.asStateFlow()

    private val _filterType = MutableStateFlow<BookFilterType>(BookFilterType.All)
    val filterType: StateFlow<BookFilterType> = _filterType.asStateFlow()

    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val navigationEventFlow = _navigationEventFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedBooksFlow = filterType
        .flatMapLatest<BookFilterType, List<BookUiModel>> { filterType ->
            bookShelfRepository.getShelfItems(filterType)
        }
        .shareIn(
            scope = viewModelScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed()
        )

    private val _selectedBookId = MutableStateFlow<Int?>(null)
    val selectedBookId: StateFlow<Int?> = _selectedBookId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedBook: StateFlow<BookUiModel?> = selectedBookId
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                bookShelfRepository.getBookById(id),
                memoRepository.getMemosByBookId(id)
            ) { book, memos -> book to memos }   // 1. book과 memos를 하나로 묶음
        }
        .flatMapLatest { (book, memos) ->
            if (memos.isEmpty()) {
                flowOf(book.copy(memoList = emptyList()))
            } else {
                combine(
                    memos.map { memo ->
                        tagRepository.getTagsByMemoId(memo.memoId)
                            .map { tags -> memo.copy(tags = tags) }
                    }
                ) { memosWithTags: Array<MemoUiModel> ->  // 2. 명시적으로 타입 지정
                    book.copy(memoList = memosWithTags.toList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    fun setSelectedBook(id: Int) {
        _selectedBookId.value = id
    }

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            sharedBooksFlow.collect { books ->
                Log.d("loadBooks TEST ", "_uiState.value: ${_uiState.value}")

                if (_uiState.value is MainViewUiState.BookDetail) {
                    return@collect
                }

                if (books.isEmpty()) {
                    _uiState.value = MainViewUiState.Empty
                } else {
                    _uiState.value = MainViewUiState.MyLibrary(books = books)
                }
            }
        }
    }

    fun refreshShelf() {
        loadBooks()
    }

    fun updateUiState(newState: MainViewUiState) {
        viewModelScope.launch {
            _uiState.value = newState
        }
    }

    fun navigateToBookDetail(selectedBook: BookUiModel, position: Int, sharedView: View?) {
        viewModelScope.launch {
            setSelectedBook(selectedBook.itemId)
            if (_uiState.value is MainViewUiState.BookDetail) {
                val currentState = _uiState.value as MainViewUiState.BookDetail
                if (currentState.currentBook.itemId == selectedBook.itemId) {
                    return@launch
                }
            }
            
            val currentBooks = when (val currentState = _uiState.value) {
                is MainViewUiState.MyLibrary -> currentState.books
                is MainViewUiState.BookDetail -> currentState.books
                else -> emptyList()
            }

            _uiState.value = MainViewUiState.BookDetail(
                books = currentBooks,
                currentBook = selectedBook,
                initialPosition = position,
            )

            _navigationEventFlow.emit(
                NavigationEvent.NavigateToBookDetail(
                    book = selectedBook,
                    position = position,
                    transitionName = "sharedView_${selectedBook.itemId}"
                )
            )
        }
    }

    suspend fun deleteSelectedBooks(books: List<BookUiModel>) {
        books.forEach { book ->
            bookShelfRepository.deleteBook(book)
        }
        refreshShelf()
    }

    fun updateFilterType(newFilterType: BookFilterType) {
        viewModelScope.launch {
            _filterType.value = newFilterType
        }
    }

    fun deleteMemoById(memoId: Long) {
        viewModelScope.launch {
            memoRepository.deleteMemoById(memoId)
        }
    }
}

sealed class NavigationEvent {
    data class NavigateToBookDetail(
        val book: BookUiModel,
        val position: Int,
        val transitionName: String
    ) : NavigationEvent()
}

sealed class BookFilterType {
    object Reading : BookFilterType()
    object Finished : BookFilterType()
    object All : BookFilterType()
}

sealed class MainViewUiState {
    data class MyLibrary(
        val books: List<BookUiModel>,
        val isLoading: Boolean = false,
        val error: String? = null,
        val transitionName: String = ""
    ) : MainViewUiState()

    data class BookDetail(
        val books: List<BookUiModel>,
        val currentBook: BookUiModel,
        val initialPosition: Int? = null,
//        val memos: List<MemoUiModel>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MainViewUiState()

    data object Loading : MainViewUiState()
    data object Empty : MainViewUiState()
}

private fun MainViewUiState.asBookDetail(): MainViewUiState.BookDetail? =
    this as? MainViewUiState.BookDetail