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
import android.widget.Toast
import com.example.bookchigibakchigi.ui.main.MainActivity

@AndroidEntryPoint
class AddMemoActivity : BaseActivity() {

    private lateinit var binding: ActivityAddMemoBinding
    private val viewModel: AddMemoViewModel by viewModels()
    private var pageDebounceJob: Job? = null
    private var contentDebounceJob: Job? = null
    private lateinit var tagListAdapter: TagListAdapter
    private lateinit var addMemoAdapter: AddMemoAdapter
    private var colorPickerDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initViewPager()
        
        // copiedText 처리
        val recognizedText = intent.getStringExtra("recognizedText")
        if (!recognizedText.isNullOrEmpty()) {
            viewModel.onEvent(AddMemoEvent.UpdateContent(recognizedText))
        }

        initListeners()
        observeViewModel()

        // 초기 색상 설정
//        binding.vColorSelector.tag = ColorConstants.COLOR_CODES[0]
    }

    private fun initViewPager() {
        addMemoAdapter = AddMemoAdapter(
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
            }
        )
        binding.viewPager.adapter = addMemoAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "페이지"
                1 -> "내용"
                2 -> "태그"
                else -> throw IllegalArgumentException("Invalid position")
            }
        }.attach()
    }

    override fun onBackPressed() {
        if (binding.colorPickerLayout.isVisible) {
            binding.flColorPallet.visibility = View.GONE
            binding.colorPickerLayout.visibility = View.INVISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun initRecyclerView() {
        tagListAdapter = TagListAdapter { tag ->
            viewModel.onEvent(AddMemoEvent.RemoveTag(tag.name))
        }

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            alignItems = AlignItems.FLEX_START
        }

//        binding.rvTagList.apply {
//            layoutManager = flexboxLayoutManager
//            adapter = tagListAdapter
//        }
    }

//    private fun selectColor(colorIndex: Int) {
//        binding.vColorSelector.backgroundTintList = android.content.res.ColorStateList.valueOf(
//            ColorConstants.COLOR_CODES[colorIndex].toColorInt()
//        )
//        binding.vColorSelector.tag = ColorConstants.COLOR_CODES[colorIndex]
//        binding.colorPickerLayout.visibility = View.INVISIBLE
//        binding.flColorPallet.visibility = View.GONE
//    }

    private fun isKeyboardVisible(): Boolean {
        val r = Rect()
        binding.root.getWindowVisibleDisplayFrame(r)
        val screenHeight = binding.root.rootView.height
        val keypadHeight = screenHeight - r.bottom
        return keypadHeight > screenHeight * 0.15
    }

//    private fun adjustColorPickerPosition() {
//        val keyboardVisible = isKeyboardVisible()
//        binding.colorPickerLayout.x = binding.tagLabel.right.toFloat() + 80
//        binding.colorPickerLayout.y = if (keyboardVisible) {
////            binding.rlAddTag.top.toFloat() - 20 - (binding.root.rootView.height * 0.3).toFloat()
//            binding.etContent.bottom.toFloat() - 200
////            binding.saveButton.top.toFloat()
//        } else {
//            binding.rlAddTag.top.toFloat() - 20
//        }
//    }

    private fun initListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

//        binding.etPage.doAfterTextChanged { text ->
//            pageDebounceJob?.cancel()
//            pageDebounceJob = lifecycleScope.launch {
//                delay(300)
//                viewModel.onEvent(AddMemoEvent.UpdatePage(text.toString()))
//            }
//        }
//
//        binding.etContent.doAfterTextChanged { text ->
//            contentDebounceJob?.cancel()
//            contentDebounceJob = lifecycleScope.launch {
//                delay(300)
//                viewModel.onEvent(AddMemoEvent.UpdateContent(text.toString()))
//            }
//        }
//
//        binding.vColorSelector.setOnClickListener {
//            binding.flColorPallet.visibility = View.VISIBLE
//            binding.colorPickerLayout.visibility = View.VISIBLE
//            adjustColorPickerPosition()
//        }
//
//        binding.btnAddTag.setOnClickListener {
//            val tagName = binding.etTag.text.toString().trim()
//            val colorCode = binding.vColorSelector.tag
//            val textColorCode = "#FFFFFF"
//            if (tagName.isNotEmpty()) {
//                // 같은 이름의 태그가 있는지 확인하고 있다면 제거
//                val existingTag = tagListAdapter.currentList.find { it.name == tagName }
//                if (existingTag != null) {
//                    viewModel.onEvent(AddMemoEvent.RemoveTag(existingTag.name))
//                }
//                // 새로운 태그 추가
//                viewModel.onEvent(AddMemoEvent.AddTag(tagName, colorCode.toString(), textColorCode))
//                binding.etTag.text.clear()
//            }
//        }

        binding.saveButton.setOnClickListener {
            val bookId = intent.getIntExtra("bookId", -1)
            if(bookId == -1) {
                Toast.makeText(this, "책 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.onEvent(AddMemoEvent.SaveMemo(bookId))
        }

        binding.flColorPallet.setOnClickListener {
            binding.colorPickerLayout.visibility = View.INVISIBLE
            binding.flColorPallet.visibility = View.GONE
        }

        // 색상 버튼 클릭 리스너 추가
//        binding.colorGray.setOnClickListener { selectColor(0) }
//        binding.colorRed.setOnClickListener { selectColor(1) }
//        binding.colorOrange.setOnClickListener { selectColor(2) }
//        binding.colorBlue.setOnClickListener { selectColor(3) }
//        binding.colorPurple.setOnClickListener { selectColor(4) }
//        binding.colorPink.setOnClickListener { selectColor(5) }
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
        binding.saveButton.isEnabled = state.page.isNotEmpty() && state.content.isNotEmpty() && !state.isLoading
        
        // 버튼의 배경색과 텍스트 색상 변경
        if (state.isContentValid) {
            binding.saveButton.setBackgroundColor(getColor(android.R.color.black))
        } else {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.saveButton.setBackgroundColor(getColor(android.R.color.darker_gray))
        }

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

        tagListAdapter.submitList(state.tagList)
        // 태그 리스트가 업데이트된 후 최하단으로 스크롤
//        binding.rvTagList.post {
//            lifecycleScope.launch {
//                delay(300) // 필요에 따라 조절
////                val scrollAmount = binding.scrollView.getChildAt(0).height
////                binding.scrollView.smoothScrollTo(0, scrollAmount)
//            }
//        }

        state.error?.let { error ->
            showError(error)
        }
    }

    private fun showError(message: String) {
        // 에러 메시지를 표시하는 로직 구현
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        colorPickerDialog?.dismiss()
    }
}