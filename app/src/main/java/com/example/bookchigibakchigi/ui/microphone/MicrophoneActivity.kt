package com.example.bookchigibakchigi.ui.microphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityMicrophoneBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.card.CardActivity
import com.example.bookchigibakchigi.util.VibrationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MicrophoneActivity : BaseActivity() {

    private lateinit var binding: ActivityMicrophoneBinding
    private val viewModel: MicrophoneViewModel by viewModels()
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
//        setupToolbar(binding.toolbar, binding.main)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        checkMicrophonePermission()
        initializeSpeechRecognizer()

        observeViewModel()

        binding.ivMicrophone.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    startListening()
                    VibrationUtil.vibrate(this, 200)
                }
                MotionEvent.ACTION_UP -> stopListening()
            }
            false
        }

        binding.tvNext.setOnClickListener {
            val copiedText = viewModel.recognizedText.value
            val bookId = intent.getIntExtra("bookId", -1)
            val intent = Intent(this, CardActivity::class.java).apply {
                putExtra("bookId", bookId)
                putExtra("copiedText", copiedText)
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isRecording.collectLatest { isRecording ->
                if (isRecording) {
                    Toast.makeText(this@MicrophoneActivity, "음성 인식 중...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    MicrophoneUiState.Recording -> {
//                        binding.ivMicrophone.setImageResource(R.drawable.ic_microphone_recording)
                    }
                    MicrophoneUiState.NotRecording -> {
//                        binding.ivMicrophone.setImageResource(R.drawable.ic_microphone)
                    }
                    MicrophoneUiState.Error -> {
                        Toast.makeText(this@MicrophoneActivity, "음성 인식 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initBinding() {
        binding = ActivityMicrophoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                viewModel.stopRecording()
                Toast.makeText(this@MicrophoneActivity, "음성 인식 실패. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                viewModel.stopRecording()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val newText = matches?.firstOrNull() ?: ""
                if (!matches.isNullOrEmpty()) {
                    viewModel.appendRecognizedText(newText)
                } else {
                    Toast.makeText(this@MicrophoneActivity, "음성 인식 실패. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        viewModel.startRecording()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "책 속 문장을 말씀하세요")
        }
        speechRecognizer.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        viewModel.stopRecording()
    }

    private fun checkMicrophonePermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "음성 인식을 사용하려면 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_memo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
