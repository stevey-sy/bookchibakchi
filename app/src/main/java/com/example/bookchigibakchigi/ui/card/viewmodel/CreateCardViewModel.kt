package com.example.bookchigibakchigi.ui.card.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.entity.CardTextEntity
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CreateCardViewModel @Inject constructor(
    private val photoCardRepository: PhotoCardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _saveResult = MutableLiveData<Result<Boolean>>() // ✅ 결과 상태 관리
    val saveResult: LiveData<Result<Boolean>> get() = _saveResult

    fun saveCard(bitmap: Bitmap, book: BookEntity, contentText: EditText, titleText: EditText, contentLayout: FrameLayout, titleLayout: FrameLayout, height: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ 내부 저장소에 이미지 저장
                val filePath = saveImageToInternalStorage(bitmap, book.isbn)

                // ✅ 포토카드 데이터 저장
                savePhotoCardDataToDatabase(filePath, book, contentText, titleText, contentLayout, titleLayout, height)

                _saveResult.postValue(Result.success(true)) // ✅ 저장 성공
            } catch (e: Exception) {
                _saveResult.postValue(Result.failure(e)) // ✅ 저장 실패
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap, isbn: String?): String {
        val fileName = "${isbn}_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, fileName) // 내부 저장소에 저장할 파일 경로 설정

        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // PNG 형식으로 저장
        outputStream.flush()
        outputStream.close()

        return file.absolutePath // ✅ 저장된 파일 경로 반환
    }

    private suspend fun savePhotoCardDataToDatabase(filePath: String, book: BookEntity, contentText: EditText, titleText: EditText, contentLayout: FrameLayout, titleLayout: FrameLayout, height: Int) {
        val imageFileName = File(filePath).name
        val createdAt = System.currentTimeMillis()

        val contentTextEntity = createCardTextEntity(contentText, contentLayout)
        val titleTextEntity = createCardTextEntity(titleText, titleLayout)

        val photoCardEntity = PhotoCardEntity(
            imageFileName = imageFileName,
            isbn = book.isbn,
            createdAt = createdAt,
            height = height,
        )

        photoCardRepository.insertPhotoCardWithTexts(photoCardEntity, listOf(contentTextEntity, titleTextEntity))
    }

    private fun createCardTextEntity(editText: EditText, frameLayout: FrameLayout): CardTextEntity {
        val content = editText.text.toString()
        val textSize = editText.textSize
        val textColorHex = String.format("#%06X", 0xFFFFFF and editText.currentTextColor)

        val backgroundDrawable = editText.background
        val backgroundColorInt = if (backgroundDrawable is ColorDrawable) {
            backgroundDrawable.color
        } else {
            Color.TRANSPARENT
        }
        val backgroundColorHex = String.format("#%06X", 0xFFFFFF and backgroundColorInt)

        return CardTextEntity(
            photoCardId = 0,
            type = "text",
            content = content,
            textColor = textColorHex,
            textSize = textSize,
            textBackgroundColor = backgroundColorHex,
            startX = frameLayout.x,
            startY = frameLayout.y,
            font = "default"
        )
    }
}