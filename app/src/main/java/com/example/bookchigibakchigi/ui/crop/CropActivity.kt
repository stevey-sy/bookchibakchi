package com.example.bookchigibakchigi.ui.crop

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityCropBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addmemo.AddMemoActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class CropActivity : BaseActivity() {
    private lateinit var binding: ActivityCropBinding
    private lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar(binding.toolbar, binding.main)
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
                moveToAddMemoActivity(recognizedText)
            }
            .addOnFailureListener { e ->
                Log.e("OCR_ERROR", "OCR 실패: ${e.message}")
                Toast.makeText(this, "텍스트 인식 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun moveToAddMemoActivity(copiedText: String) {
        val intent = Intent(this, AddMemoActivity::class.java).apply {
            putExtra("recognizedText", copiedText)
            putExtra("bookId", intent.getIntExtra("bookId", -1))
        }
        startActivity(intent)
    }
}