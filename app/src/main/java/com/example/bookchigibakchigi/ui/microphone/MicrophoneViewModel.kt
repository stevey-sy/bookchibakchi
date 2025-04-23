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

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    fun startRecording() {
        _uiState.value = MicrophoneUiState.Recording
    }

    fun stopRecording() {
        _uiState.value = MicrophoneUiState.NotRecording
    }

    fun appendRecognizedText(newText: String) {
        val currentText = _recognizedText.value
        _recognizedText.value = if (currentText.isNotEmpty()) {
            "$currentText\n$newText"
        } else {
            newText
        }
    }
}

sealed interface MicrophoneUiState {
    data object NotRecording : MicrophoneUiState
    data object Recording : MicrophoneUiState
    data object Error : MicrophoneUiState
}
