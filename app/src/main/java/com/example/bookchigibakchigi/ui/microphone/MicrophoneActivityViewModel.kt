package com.example.bookchigibakchigi.ui.microphone

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.util.VibrationUtil

class MicrophoneActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _currentBook = MutableLiveData<BookEntity>()
    val currentBook: LiveData<BookEntity> = _currentBook
    fun setCurrentBook(book: BookEntity) {
        _currentBook.value = book
    }

    private val _isRecording = MutableLiveData<Boolean>().apply { value = false }
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _headerText = MutableLiveData<String>()
    val headerText: LiveData<String> get() = _headerText

    private val _textColor = MutableLiveData<Int>()
    val textColor: LiveData<Int> get() = _textColor

    private val _backgroundColor = MutableLiveData<Int>(R.color.white) // 초기 배경색: 흰색
    val backgroundColor: LiveData<Int> get() = _backgroundColor

    private val _recognizedText = MutableLiveData<String>().apply { value = "" } // 초기 텍스트는 빈 값
    val recognizedText: LiveData<String> get() = _recognizedText

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)

    init {
        // 초기 상태 설정
        updateHeaderState()

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                _isRecording.value = false
                updateHeaderState()
                // Toast 로 "음성 인식 실패. 다시 시도해 주세요." 짧게 보여주기
                Toast.makeText(context, "음성 인식 실패. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                _isRecording.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val newText = matches?.firstOrNull() ?: ""
                if(matches.isNullOrEmpty()) {
                    Toast.makeText(context, "음성 인식 실패. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    appendRecognizedText(newText)
                }
                updateHeaderState()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        _isRecording.value = true
        updateHeaderState()
        VibrationUtil.vibrate(context, 200)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 한국어 설정
            putExtra(RecognizerIntent.EXTRA_PROMPT, "책 속 문장을 말씀하세요")
        }
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    /**
     * 음성 인식 결과를 누적하여 저장하는 함수
     */
    private fun appendRecognizedText(newText: String) {
        val currentText = _recognizedText.value ?: "" // 기존 값 가져오기
        _recognizedText.value = if (currentText.isNotEmpty()) {
            "$currentText\n$newText" // 기존 텍스트 + 줄바꿈 후 새로운 텍스트 추가
        } else {
            newText // 기존 텍스트가 비어있다면 그냥 추가
        }
    }

    private fun updateHeaderState() {
        if (_isRecording.value == true) {
            _backgroundColor.value = R.color.black
            _headerText.value = "목소리를 듣고 있습니다..."
            _textColor.value = ContextCompat.getColor(context, R.color.white)
        } else {
            _backgroundColor.value = R.color.white
            _headerText.value = "버튼을 꾹 누른 상태에서\n기록하고 싶은 문구를 읽어주세요"
            _textColor.value = ContextCompat.getColor(context, R.color.black)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
    }
}
