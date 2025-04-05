package com.example.bookchigibakchigi.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.data.entity.BookEntity
import kotlinx.coroutines.launch
import kotlin.math.min

class PageInputDialog(
    context: Context,
    private val currentBook: BookEntity,
    private val onComplete: (Int) -> Unit,
    private val onAllComplete: () -> Unit
) {
    private val dialog = Dialog(context)
    private val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_record_complete, null)

    init {
        setupDialog()
        setupViews()
        setupListeners()
    }

    private fun setupDialog() {
        // 다이얼로그의 둥근 테두리를 적용 (배경 투명)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setContentView(dialogView)
        
        // Dialog 좌우 margin 적용 (10dp 여백)
        val params = dialog.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.horizontalMargin = 0.1f
        dialog.window?.attributes = params
    }

    private fun setupViews() {
        val etPageInput = dialogView.findViewById<EditText>(R.id.etPageInput)
        val tvTotalPages = dialogView.findViewById<TextView>(R.id.tvTotalPages)

        etPageInput.setText(currentBook.currentPageCnt.toString())
        tvTotalPages.text = "/ ${currentBook.totalPageCnt}"
    }

    private fun setupListeners() {
        val etPageInput = dialogView.findViewById<EditText>(R.id.etPageInput)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirm)
        val btnAllComplete = dialogView.findViewById<TextView>(R.id.btnAllComplete)

        btnAllComplete.setOnClickListener {
            onAllComplete()
        }

        btnCancel.setOnClickListener { 
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val enteredText = etPageInput.text.toString()
            if (enteredText.isNotEmpty()) {
                var enteredPage = enteredText.toInt()
                enteredPage = min(enteredPage, currentBook.totalPageCnt)
                dialog.dismiss()
                onComplete(enteredPage)
            }
        }

        // 다이얼로그가 표시된 후 키보드를 자동으로 띄우고, EditText 전체 선택
        Handler(Looper.getMainLooper()).postDelayed({
            etPageInput.requestFocus()
            etPageInput.selectAll()

            val imm = dialog.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etPageInput, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    fun show() {
        dialog.show()
    }
} 