package com.example.bookchigibakchigi.ui.addmemo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.widget.doAfterTextChanged
import com.example.bookchigibakchigi.constants.ColorConstants
import com.example.bookchigibakchigi.databinding.ActivityAddMemoBinding
import com.example.bookchigibakchigi.databinding.DialogColorPickerBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addmemo.adapter.AddMemoAdapter
import com.example.bookchigibakchigi.ui.addmemo.adapter.TagListAdapter
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import androidx.core.graphics.toColorInt
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import com.example.bookchigibakchigi.ui.main.MainActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.constants.CardBackgrounds
import com.example.bookchigibakchigi.util.VibrationUtil
import com.example.bookchigibakchigi.ui.dialog.TwoButtonsDialog

@AndroidEntryPoint
class AddMemoActivity : BaseActivity() {

    private lateinit var binding: ActivityAddMemoBinding
    private val viewModel: AddMemoViewModel by viewModels()
    private lateinit var addMemoAdapter: AddMemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // copiedText 처리
        var recognizedText = intent.getStringExtra("recognizedText")
        if (!recognizedText.isNullOrEmpty()) {
            viewModel.onEvent(AddMemoEvent.UpdateContent(recognizedText))
        }
        initViewPager(recognizedText)
        initListeners()
        observeViewModel()
    }

    private fun initViewPager(initialContent : String?) {
        
        addMemoAdapter = AddMemoAdapter(
            initialContent = initialContent ?: "",
            onBackgroundChanged = { position ->
                // 배경이 변경되었을 때의 처리
                Log.d("onBackgroundChanged", position.toString())
                VibrationUtil.vibrate(this@AddMemoActivity, 100)
                viewModel.onEvent(AddMemoEvent.UpdateBackground(position))
            },
            onPageChanged = { page ->
                viewModel.onEvent(AddMemoEvent.UpdatePage(page))
            },
            onContentChanged = { content ->
                viewModel.onEvent(AddMemoEvent.UpdateContent(content))
            },
            onTagAdded = { tagName ->
                viewModel.onEvent(AddMemoEvent.AddTag(
                    tagName,
                    "#000000", // 기본 색상
                    "#FFFFFF"  // 기본 텍스트 색상
                ))
                // 태그 추가 후 스크롤을 최하단으로 이동
                binding.scrollView.post {
                    val scrollAmount = binding.scrollView.getChildAt(0).height
                    binding.scrollView.smoothScrollTo(0, scrollAmount)
                }
            },
            onTagRemoved = { tagName ->
                viewModel.onEvent(AddMemoEvent.RemoveTag(tagName))
            },
            backgroundImages = CardBackgrounds.IMAGE_LIST
        )
        binding.viewPager.adapter = addMemoAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager.isUserInputEnabled = false
        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    private fun initListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnPrev.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.setCurrentItem(currentItem - 1, true)
            }
        }

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            val itemCount = binding.viewPager.adapter?.itemCount ?: 0
            if (currentItem < itemCount - 1) {
                binding.viewPager.setCurrentItem(currentItem + 1, true)
            } else {
                // 마지막 페이지에서 저장 확인 다이얼로그 표시
                TwoButtonsDialog(
                    context = this,
                    title = "메모 저장",
                    msg = "메모를 저장하시겠습니까?",
                    btnText1 = "취소",
                    btnText2 = "저장",
                    onBtn1Click = {
                        // 취소 시 아무 동작 없음
                    },
                    onBtn2Click = {
                        val bookId = intent.getIntExtra("bookId", -1)
                        viewModel.onEvent(AddMemoEvent.SaveMemo(bookId))
                    }
                ).show()
            }
        }

        binding.flColorPallet.setOnClickListener {
            binding.colorPickerLayout.visibility = View.INVISIBLE
            binding.flColorPallet.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AddMemoUiState) {
        // 페이지 번호 업데이트
        if (state.isSuccess) {
            Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            if (state.shouldNavigateToMain) {
                val bookId = intent.getIntExtra("bookId", -1)
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("bookId", bookId)
                }
                startActivity(intent)
                finish()
            }
        }

        state.error?.let { error ->
            showError(error)
        }
    }

    private fun showError(message: String) {
        // 에러 메시지를 표시하는 로직 구현
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}