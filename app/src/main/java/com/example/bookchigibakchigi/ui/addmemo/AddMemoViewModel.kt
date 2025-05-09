package com.example.bookchigibakchigi.ui.addmemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.constants.CardBackgrounds
import com.example.bookchigibakchigi.data.entity.MemoEntity
import com.example.bookchigibakchigi.data.entity.TagEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.MemoRepository
import com.example.bookchigibakchigi.data.repository.TagRepository
import com.example.bookchigibakchigi.mapper.TagMapper
import com.example.bookchigibakchigi.model.AddMemoFormUiModel
import com.example.bookchigibakchigi.model.TagUiModel
import com.example.bookchigibakchigi.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

//data class AddMemoUiState(
//    val isModify: Boolean = false,
//    val page: String = "0",
//    val backgroundPosition: Int = 2,
//    val content: String = "내용을 입력해 주세요.",
//    val tagList: List<TagUiModel> = emptyList(),
//    val createdAt: String = DateUtil.getFormattedToday(),
//    val isLoading: Boolean = false,
//    val isSuccess: Boolean = false,
//    val error: String? = null,
//    val isContentValid: Boolean = false,
//    val shouldNavigateToMain: Boolean = false,
//    val memoId: Long? = null,
//    val bookId: Int? = null,
//)

sealed class AddMemoUiState {
    data object Loading : AddMemoUiState()
    data class CreateMode(val form: AddMemoFormUiModel) : AddMemoUiState()
    data class EditMode(val form: AddMemoFormUiModel) : AddMemoUiState()
    data object Success : AddMemoUiState()
    data class Error(val message: String) : AddMemoUiState()
}

sealed interface AddMemoEvent {
    data class UpdatePage(val page: String) : AddMemoEvent
    data class UpdateContent(val content: String) : AddMemoEvent
    data class AddTag(val name: String, val backgroundColor: String, val textColor: String) : AddMemoEvent
    data class RemoveTag(val tagName: String) : AddMemoEvent
    data class SaveMemo(val bookId: Int) : AddMemoEvent
    object UpdateMemo : AddMemoEvent
    data class UpdateBackground(val position: Int) : AddMemoEvent
    data class LoadMemo(val bookId: Int, val memoId: Long) : AddMemoEvent
}

@HiltViewModel
class AddMemoViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
    private val tagRepository: TagRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<AddMemoUiState>(AddMemoUiState.CreateMode(AddMemoFormUiModel()))
    val uiState: StateFlow<AddMemoUiState> = _uiState.asStateFlow()

    fun setCreateMode(memoContent: String?) {
        _uiState.value = AddMemoUiState.CreateMode(AddMemoFormUiModel(content = memoContent ?: ""))
    }

    fun onEvent(event: AddMemoEvent) {
        when (event) {
            is AddMemoEvent.LoadMemo -> {
                loadMemo(event.memoId, event.bookId)
            }
            is AddMemoEvent.UpdateBackground -> {
                updateForm { it.copy(backgroundPosition = event.position) }
            }
            is AddMemoEvent.UpdatePage -> {
                updateForm { it.copy(page = event.page) }
            }
            is AddMemoEvent.UpdateContent -> {
                updateForm { it.copy(
                    content = event.content,
                    isContentValid = event.content.trim().isNotEmpty()
                ) }
            }
            is AddMemoEvent.AddTag -> {
                val newTag = TagEntity(
                    name = event.name,
                    backgroundColor = event.backgroundColor,
                    textColor = event.textColor,
                )
                val newTagUiModel = TagMapper.toUiModel(newTag)
                updateForm { currentForm ->
                    if (!currentForm.tagList.any { it.name == newTagUiModel.name }) {
                        currentForm.copy(tagList = currentForm.tagList + newTagUiModel)
                    } else {
                        currentForm
                    }
                }
            }
            is AddMemoEvent.RemoveTag -> {
                updateForm { currentForm ->
                    currentForm.copy(
                        tagList = currentForm.tagList.filter { it.name != event.tagName }
                    )
                }
            }
            is AddMemoEvent.SaveMemo -> {
                saveMemo(event.bookId)
            }
            is AddMemoEvent.UpdateMemo -> {
                updateMemo()
            }
        }
    }

    private fun updateForm(update: (AddMemoFormUiModel) -> AddMemoFormUiModel) {
        _uiState.update { currentState ->
            when (currentState) {
                is AddMemoUiState.CreateMode -> AddMemoUiState.CreateMode(update(currentState.form))
                is AddMemoUiState.EditMode -> AddMemoUiState.EditMode(update(currentState.form))
                else -> currentState
            }
        }
    }

    private fun loadMemo(memoId: Long, bookId: Int) {
        viewModelScope.launch {
            _uiState.value = AddMemoUiState.Loading
            try {
                val memo = memoRepository.getMemoById(memoId)
                val tags = memoRepository.getTagsForMemo(memoId).first()
                
                val form = AddMemoFormUiModel(
                    isModify = true,
                    page = memo!!.pageNumber.toString(),
                    content = memo.content,
                    backgroundPosition = CardBackgrounds.IMAGE_LIST.indexOf(memo.background),
                    tagList = tags.map { TagMapper.toUiModel(it) },
                    createdAt = DateUtil.formatDateFromMillis(memo.createdAt),
                    memoId = memoId,
                    bookId = bookId
                )
                _uiState.value = AddMemoUiState.EditMode(form)
            } catch (e: Exception) {
                _uiState.value = AddMemoUiState.Error(e.message ?: "메모 로드 중 오류가 발생했습니다.")
            }
        }
    }

    private fun saveMemo(bookId: Int) {
        viewModelScope.launch {
//            _uiState.value = AddMemoUiState.Loading
            try {
                val form = when (val currentState = _uiState.value) {
                    is AddMemoUiState.CreateMode -> currentState.form
                    is AddMemoUiState.EditMode -> currentState.form
                    else -> return@launch
                }

                val tagEntities = form.tagList.map { TagMapper.toEntity(it) }
                
                // 태그 저장
                val savedTagIds = mutableListOf<Long>()
                for (tag in tagEntities) {
                    val tagId = tagRepository.insertTag(tag)
                    savedTagIds.add(tagId)
                }

                // 메모 저장
                val memo = MemoEntity(
                    bookId = bookId,
                    content = form.content,
                    pageNumber = form.page.toIntOrNull() ?: 0,
                    background = CardBackgrounds.IMAGE_LIST[form.backgroundPosition],
                    createdAt = System.currentTimeMillis()
                )
                val memoId = memoRepository.insertMemo(memo)

                // 메모-태그 관계 저장
                for (tagId in savedTagIds) {
                    memoRepository.addTagToMemo(memoId, tagId)
                }

                _uiState.value = AddMemoUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddMemoUiState.Error(e.message ?: "메모 저장 중 오류가 발생했습니다.")
            }
        }
    }

    private fun updateMemo() {
        viewModelScope.launch {
            _uiState.value = AddMemoUiState.Loading
            try {
                val form = when (val currentState = _uiState.value) {
                    is AddMemoUiState.EditMode -> currentState.form
                    else -> return@launch
                }

                // 현재 UI의 태그 목록
                val currentTagEntities = form.tagList.map { TagMapper.toEntity(it) }
                
                // 기존 태그 목록 가져오기
                val existingTags = memoRepository.getTagsForMemo(form.memoId!!).first()
                
                // 태그 변경 사항 확인
                val tagsToAdd = currentTagEntities.filter { newTag ->
                    !existingTags.any { existingTag -> existingTag.name == newTag.name }
                }
                
                val tagsToRemove = existingTags.filter { existingTag ->
                    !currentTagEntities.any { newTag -> newTag.name == existingTag.name }
                }

                // 메모 업데이트
                val updatedMemo = MemoEntity(
                    memoId = form.memoId!!,
                    bookId = form.bookId!!,
                    content = form.content,
                    pageNumber = form.page.toIntOrNull() ?: 0,
                    background = CardBackgrounds.IMAGE_LIST[form.backgroundPosition],
                    createdAt = DateUtil.getMillisFromDate(form.createdAt)
                )
                memoRepository.updateMemo(updatedMemo)

                // 새로운 태그 저장 및 관계 추가
                for (tag in tagsToAdd) {
                    val tagId = tagRepository.insertTag(tag)
                    memoRepository.addTagToMemo(updatedMemo.memoId, tagId)
                }

                // 제거된 태그 관계 삭제
                for (tag in tagsToRemove) {
                    memoRepository.removeTagFromMemo(updatedMemo.memoId, tag.tagId)
                }

                _uiState.value = AddMemoUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddMemoUiState.Error(e.message ?: "메모 수정 중 오류가 발생했습니다.")
            }
        }
    }
}