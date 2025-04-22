package com.example.bookchigibakchigi.ui.microphone

import androidx.lifecycle.ViewModel
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.ui.record.RecordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MicrophoneViewModel @Inject constructor(
    private val bookShelfRepository: BookShelfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MicrophoneUiState>(MicrophoneUiState.NotRecording)
    val uiState: StateFlow<MicrophoneUiState> = _uiState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _headerText = MutableStateFlow("")
    val headerText: StateFlow<String> = _headerText.asStateFlow()

    private val _textColor = MutableStateFlow(0)
    val textColor: StateFlow<Int> = _textColor.asStateFlow()

    private val _backgroundColor = MutableStateFlow(R.color.white)
    val backgroundColor: StateFlow<Int> = _backgroundColor.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    fun startRecording() {
        _isRecording.value = true
        _uiState.value = MicrophoneUiState.Recording
        updateHeaderState()
    }

    fun stopRecording() {
        _isRecording.value = false
        _uiState.value = MicrophoneUiState.NotRecording
        updateHeaderState()
    }

    fun appendRecognizedText(newText: String) {
        val currentText = _recognizedText.value
        _recognizedText.value = if (currentText.isNotEmpty()) {
            "$currentText\n$newText"
        } else {
            newText
        }
    }

    private fun updateHeaderState() {
        if (_isRecording.value) {
            _backgroundColor.value = R.color.black
            _headerText.value = "목소리를 듣고 있습니다..."
            _textColor.value = R.color.white
        } else {
            _backgroundColor.value = R.color.white
            _headerText.value = "버튼을 꾹 누른 상태에서\n기록하고 싶은 문구를 읽어주세요"
            _textColor.value = R.color.black
        }
    }
}

sealed interface MicrophoneUiState {
    data object NotRecording : MicrophoneUiState
    data object Recording : MicrophoneUiState
    data object Error : MicrophoneUiState
}
