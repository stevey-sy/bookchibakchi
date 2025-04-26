package com.example.bookchigibakchigi.ui.addmemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.data.entity.TagEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.MemoRepository
import com.example.bookchigibakchigi.data.repository.TagRepository
import com.example.bookchigibakchigi.mapper.TagMapper
import com.example.bookchigibakchigi.model.TagUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMemoUiState(
    val page: String = "",
    val content: String = "",
    val tagList: List<TagUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isContentValid: Boolean = false
)

sealed interface AddMemoEvent {
    data class UpdatePage(val page: String) : AddMemoEvent
    data class UpdateContent(val content: String) : AddMemoEvent
    data class AddTag(val name: String, val backgroundColor: String, val textColor: String) : AddMemoEvent
    data class RemoveTag(val tagName: String) : AddMemoEvent
    data class SaveMemo(val bookId: Int) : AddMemoEvent
}

@HiltViewModel
class AddMemoViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
    private val tagRepository: TagRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AddMemoUiState())
    val uiState: StateFlow<AddMemoUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddMemoEvent) {
        when (event) {
            is AddMemoEvent.UpdatePage -> {
                _uiState.update { it.copy(page = event.page) }
            }
            is AddMemoEvent.UpdateContent -> {
                _uiState.update { it.copy(
                    content = event.content,
                    isContentValid = event.content.trim().isNotEmpty()
                ) }
            }
            is AddMemoEvent.AddTag -> {
                val currentTags = _uiState.value.tagList.toMutableList()
                val newTag = TagEntity(
                    name = event.name,
                    backgroundColor = event.backgroundColor,
                    textColor = event.textColor
                )
                val newTagUiModel = TagMapper.toUiModel(newTag)
                if (!currentTags.any { it.name == newTagUiModel.name }) {
                    currentTags.add(newTagUiModel)
                    _uiState.update { it.copy(tagList = currentTags) }
                }
            }
            is AddMemoEvent.RemoveTag -> {
                val currentTags = _uiState.value.tagList.toMutableList()
                currentTags.removeIf { it.name == event.tagName }
                _uiState.update { it.copy(tagList = currentTags) }
            }
            is AddMemoEvent.SaveMemo -> {
                saveMemo(event.bookId)
            }
        }
    }

    private fun saveMemo(bookId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tagEntities = _uiState.value.tagList.map { TagMapper.toEntity(it) }
                
                // 태그 저장
                val savedTagIds = mutableListOf<Long>()
                for (tag in tagEntities) {
                    val tagId = tagRepository.insertTag(tag)
                    savedTagIds.add(tagId)
                }

                // 메모 저장
                val memo = MemoEntity(
                    bookId = bookId,
                    content = _uiState.value.content,
                    pageNumber = _uiState.value.page.toIntOrNull() ?: 0
                )
                val memoId = memoRepository.insertMemo(memo)

                // 메모-태그 관계 저장
                for (tagId in savedTagIds) {
                    memoRepository.addTagToMemo(memoId, tagId)
                }

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "메모 저장 중 오류가 발생했습니다."
                ) }
            }
        }
    }
}