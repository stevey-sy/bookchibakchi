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
import com.example.bookchigibakchigi.util.VibrationUtil

@AndroidEntryPoint
class AddMemoActivity : BaseActivity() {

    private lateinit var binding: ActivityAddMemoBinding
    private val viewModel: AddMemoViewModel by viewModels()
    private lateinit var tagListAdapter: TagListAdapter
    private lateinit var addMemoAdapter: AddMemoAdapter
    private var colorPickerDialog: Dialog? = null

    // 실제 데이터 리스트
    private val actualImages = listOf(
        R.drawable.img_dummy,
        R.drawable.img_dummy,
        R.drawable.white_paper,
        R.drawable.crumpled_paper,
        R.drawable.dock_sleeping,
        R.drawable.img_pink_sky,
        R.drawable.img_forest,
        R.drawable.img_white_wall,
        R.drawable.img_gray_wall,
        R.drawable.img_blue_wall,
        R.drawable.img_dummy,
        R.drawable.img_dummy,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        initRecyclerView()
        initViewPager()
        initListeners()
        observeViewModel()

        // copiedText 처리
        val recognizedText = intent.getStringExtra("recognizedText")
        if (!recognizedText.isNullOrEmpty()) {
            viewModel.onEvent(AddMemoEvent.UpdateContent(recognizedText))
        }

    }

    private fun initViewPager() {
        addMemoAdapter = AddMemoAdapter(
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
            },
            backgroundImages = actualImages
        )
        binding.viewPager.adapter = addMemoAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager.isUserInputEnabled = false
        binding.dotsIndicator.attachTo(binding.viewPager)
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
            }
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
        // 페이지 번호 업데이트
//        binding.tvPageNumber.text = if (state.page.isNotEmpty()) "P.${state.page}" else "P.0"

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