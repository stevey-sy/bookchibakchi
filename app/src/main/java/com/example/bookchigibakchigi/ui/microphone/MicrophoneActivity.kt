package com.example.bookchigibakchigi.ui.microphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.bookchigibakchigi.ui.addmemo.AddMemoActivity
import com.example.bookchigibakchigi.ui.card.CardActivity
import com.example.bookchigibakchigi.util.SpeechRecognizerUtil
import com.example.bookchigibakchigi.util.VibrationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MicrophoneActivity : BaseActivity() {

    private lateinit var binding: ActivityMicrophoneBinding
    private val viewModel: MicrophoneViewModel by viewModels()
    private lateinit var speechRecognizerUtil: SpeechRecognizerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initClickListeners()
        checkMicrophonePermission()
        initializeSpeechRecognizer()
//        observeViewModel()
    }

    private fun initBinding() {
        binding = ActivityMicrophoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun initClickListeners() {
        binding.btnClose.setOnClickListener { finish() }

        binding.ivMicrophone.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startListening()
                }
                MotionEvent.ACTION_UP -> stopListening()
            }
            false
        }

        binding.tvNext.setOnClickListener {
            val recognizedText = viewModel.recognizedText.value
             if (recognizedText.isEmpty()) {
                 Toast.makeText(this, "인식된 텍스트가 없습니다.", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
             }
            val bookId = intent.getIntExtra("bookId", -1)
            val intent = Intent(this, AddMemoActivity::class.java).apply {
                putExtra("bookId", bookId)
                putExtra("recognizedText", recognizedText)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizerUtil = SpeechRecognizerUtil(
            context = this,
            onTextRecognized = { text ->
                viewModel.stopRecording()
                viewModel.appendRecognizedText(text)
            },
            onError = {
                viewModel.stopRecording()
            }
        )
        speechRecognizerUtil.initialize()
    }

    private fun startListening() {
        VibrationUtil.vibrate(this, 200)
        viewModel.startRecording()
        speechRecognizerUtil.startListening()
    }

    private fun stopListening() {
        viewModel.stopRecording()
        speechRecognizerUtil.stopListening()
    }

//    private fun observeViewModel() {
//        lifecycleScope.launch {
//            viewModel.uiState.collectLatest { state ->
//                when (state) {
//                    MicrophoneUiState.Recording -> {
//                        window.statusBarColor = ContextCompat.getColor(this@MicrophoneActivity, R.color.black)
//                    }
//                    MicrophoneUiState.NotRecording -> {
//                        window.statusBarColor = ContextCompat.getColor(this@MicrophoneActivity, R.color.white)
//                    }
//                    MicrophoneUiState.Error -> {
//                        window.statusBarColor = ContextCompat.getColor(this@MicrophoneActivity, R.color.white)
//                        Toast.makeText(this@MicrophoneActivity, "음성 인식 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }


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

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizerUtil.destroy()
    }
}
