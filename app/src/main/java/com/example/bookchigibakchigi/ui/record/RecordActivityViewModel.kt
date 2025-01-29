package com.example.bookchigibakchigi.ui.record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordActivityViewModel : ViewModel() {

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _timerText = MutableLiveData<String>()
    val timerText: LiveData<String> get() = _timerText

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> get() = _status
}