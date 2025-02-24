package com.example.bookchigibakchigi.ui.microphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityMicrophoneBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.card.CardActivity

class MicrophoneActivity : BaseActivity() {

    private lateinit var binding: ActivityMicrophoneBinding
    private val viewModel: MicrophoneActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMicrophoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbar, binding.main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkMicrophonePermission()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val book: BookEntity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentBook", BookEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("currentBook")
        }

        book?.let {
            viewModel.setCurrentBook(it)  // LiveData 업데이트
        }

        // 마이크 버튼 이벤트 설정
        binding.ivMicrophone.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> viewModel.startListening()
                MotionEvent.ACTION_UP -> viewModel.stopListening()
            }
            false
        }

        binding.tvNext.setOnClickListener {
            val copiedText = viewModel.recognizedText.value
            val currentBook = viewModel.currentBook.value
            val intent = Intent(this, CardActivity::class.java).apply {
                putExtra("copiedText", copiedText)
                putExtra("currentBook", currentBook)
            }

            startActivity(intent)
        }
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
}
