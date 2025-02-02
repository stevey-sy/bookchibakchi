package com.example.bookchigibakchigi.ui.record

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.databinding.ActivityRecordBinding
import com.example.bookchigibakchigi.repository.BookShelfRepository
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.MainActivityViewModel
import com.example.bookchigibakchigi.ui.MainActivityViewModelFactory
import kotlinx.coroutines.launch

class RecordActivity : BaseActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.sharedElementEnterTransition = createSharedElementTransition()
        window.sharedElementReturnTransition = createSharedElementTransition()

        enableEdgeToEdge()

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportPostponeEnterTransition()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val book: BookEntity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentBook", BookEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("currentBook")
        }

        // ✅ ViewModel에 데이터 저장
        book?.let {
            viewModel.setCurrentBook(it)  // LiveData 업데이트
            binding.ivBookCover.transitionName = "sharedView_${it.itemId}"
            binding.ivBookCover.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.ivBookCover.viewTreeObserver.removeOnPreDrawListener(this)
                    supportStartPostponedEnterTransition()  // ✅ 애니메이션 시작
                    viewModel.toggleTimer()
                    return true
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnComplete.setOnClickListener {
            viewModel.pauseTimer()
            viewModel.currentBook.value?.let { it1 ->
                showPageInputDialog(it1.currentPageCnt, it1.totalPageCnt)
            }
        }
    }

    private fun showPageInputDialog(currentPageCnt: Int, totalPageCount: Int) {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_record_complete, null)

        // ✅ 다이얼로그의 둥근 테두리를 적용 (배경 투명)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setContentView(dialogView)
        // ✅ Dialog 좌우 margin 적용 (10dp 여백)
        val params = dialog.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT // 너비는 match_parent
        params?.horizontalMargin = 0.1f // 좌우 margin (10dp 정도의 비율 적용)
        dialog.window?.attributes = params
        // ✅ Dialog 너비를 match_parent로 설정
//        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etPageInput = dialogView.findViewById<EditText>(R.id.etPageInput)
        val tvTotalPages = dialogView.findViewById<TextView>(R.id.tvTotalPages)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirm)
        val btnAllComplete = dialogView.findViewById<TextView>(R.id.btnAllComplete)

        etPageInput.setText(currentPageCnt.toString())  // 현재 읽은 페이지 기본 입력
        tvTotalPages.text = "/ $totalPageCount" // 총 페이지 수 표시

        btnAllComplete.setOnClickListener{
            lifecycleScope.launch {  // ✅ CoroutineScope 내에서 suspend 함수 호출
                val bookDao = AppDatabase.getDatabase(this@RecordActivity).bookDao()
                bookDao.updateCurrentPage(viewModel.currentBook.value!!.itemId, totalPageCount)
                dialog.dismiss()
                finish()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val enteredText = etPageInput.text.toString()
            if (enteredText.isNotEmpty()) {
                val enteredPage = enteredText.toInt()
                lifecycleScope.launch {  // ✅ CoroutineScope 내에서 suspend 함수 호출
                    val bookDao = AppDatabase.getDatabase(this@RecordActivity).bookDao()
                    bookDao.updateCurrentPage(viewModel.currentBook.value!!.itemId, enteredPage)
                    dialog.dismiss()
                    finish()
                }
            }
        }

        dialog.show()

        // ✅ 다이얼로그가 표시된 후 키보드를 자동으로 띄우고, EditText 전체 선택
        Handler(Looper.getMainLooper()).postDelayed({
            etPageInput.requestFocus() // 포커스 설정
            etPageInput.selectAll() // 텍스트 전체 선택

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etPageInput, InputMethodManager.SHOW_IMPLICIT) // 키보드 표시
        }, 100)
    }

    private fun createSharedElementTransition(): Transition {
        return TransitionInflater.from(this)
            .inflateTransition(R.transition.image_shared_element_transition).apply {
                duration = 500  // 애니메이션 지속 시간 (ms)
                interpolator = AccelerateDecelerateInterpolator()  // 부드러운 가속/감속 효과
            }
    }
}