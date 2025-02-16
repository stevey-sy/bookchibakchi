package com.example.bookchigibakchigi.ui.crop

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityCropBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class CropActivity : BaseActivity() {
    private lateinit var binding: ActivityCropBinding
    private lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar(binding.toolbar, binding.main)
        // Intent로 전달받은 Uri 가져오기
        val uriString = intent.getStringExtra("IMAGE_URI")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
        }

        binding.cropImageView.setImageUriAsync(imageUri)

        binding.tvConfirm.setOnClickListener {
            binding.cropImageView.croppedImageAsync()
        }

        binding.tvCancel.setOnClickListener {
            finish()
        }

        binding.cropImageView.setOnCropImageCompleteListener { view, result ->
            if (result.isSuccessful) {
                val croppedBitmap = result.bitmap
                // 크롭된 이미지 처리
                if (croppedBitmap != null) {
                    processImageForOCR(croppedBitmap) // 크롭된 이미지를 처리
                }
            } else {
                Log.e("CropError", "크롭 실패: ${result.error}")
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_get_text, menu) // 메뉴 파일 연결
        return true
    }

    // 메뉴 아이템 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun processImageForOCR(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.textBlocks.isEmpty()) {
                    Toast.makeText(this, "텍스트를 인식하지 못했습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val recognizedText = visionText.text
                Log.d("OCR_RESULT", "인식된 텍스트: $recognizedText")
                showCustomDialog(recognizedText)
            }
            .addOnFailureListener { e ->
                Log.e("OCR_ERROR", "OCR 실패: ${e.message}")
                Toast.makeText(this, "텍스트 인식 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCustomDialog(recognizedText: String) {
        // Dialog 생성
        val dialog = android.app.AlertDialog.Builder(this).create()

        // Custom Layout Inflate
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_result, null)

        // 레이아웃 내 View 참조
        val etRecognizedText = dialogView.findViewById<EditText>(R.id.etRecognizedText)
        val btnRetry = dialogView.findViewById<TextView>(R.id.btnRetry)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirm)

        // 인식된 텍스트를 EditText에 설정
        etRecognizedText.setText(recognizedText)

        // 복사 버튼 클릭 이벤트
        btnRetry.setOnClickListener {
//            Toast.makeText(this, "텍스트가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 닫기 버튼 클릭 이벤트
        btnConfirm.setOnClickListener {
            dialog.dismiss()
        }

        // Dialog에 커스텀 뷰 설정
        dialog.setView(dialogView)
        dialog.show()
    }
}