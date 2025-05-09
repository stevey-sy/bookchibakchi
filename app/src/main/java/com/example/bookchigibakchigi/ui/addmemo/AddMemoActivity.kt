package com.example.bookchigibakchigi.ui.addmemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bookchigibakchigi.databinding.ActivityAddMemoBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addmemo.adapter.AddMemoAdapter

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.constants.CardBackgrounds
import com.example.bookchigibakchigi.model.TagUiModel
import com.example.bookchigibakchigi.util.VibrationUtil
import com.example.bookchigibakchigi.ui.dialog.TwoButtonsDialog
import kotlinx.coroutines.flow.collectLatest

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


        // 진입 시 intent 로 부터 editMode 인지, createMode 인지 구분.
        val isEditMode = intent.getBooleanExtra("isEditMode", false)
        if(isEditMode) {
            val bookId = intent.getIntExtra("bookId", -1)
            val memoId = intent.getLongExtra("memoId", -1)
            if (memoId != -1L) {
                viewModel.onEvent(AddMemoEvent.LoadMemo(bookId, memoId))
            }
        } else {
            // createMode 인 경우.
            var recognizedText = intent.getStringExtra("recognizedText")
            viewModel.setCreateMode(recognizedText)
        }

        initListeners()
        observeUiState()
    }

    private fun initViewPager(
        content: String,
        page: String = "",
        backgroundPosition: Int = 2,
        tags: List<TagUiModel> = emptyList()
    ) {
        addMemoAdapter = AddMemoAdapter(
            initialContent = content,
            initialPage = page,
            initialBackgroundPosition = backgroundPosition,
            initialTags = tags,
            onBackgroundChanged = { position ->
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
                viewModel.onEvent(AddMemoEvent.AddTag(tagName, "#000000", "#FFFFFF"))
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
                        if (viewModel.uiState.value is AddMemoUiState.EditMode) {
                            viewModel.onEvent(AddMemoEvent.UpdateMemo)
                        } else {
                            viewModel.onEvent(AddMemoEvent.SaveMemo(bookId))
                        }
                    }
                ).show()
            }
        }

        binding.flColorPallet.setOnClickListener {
            binding.colorPickerLayout.visibility = View.INVISIBLE
            binding.flColorPallet.visibility = View.GONE
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is AddMemoUiState.Loading -> {
                            // shimmer
                        }
                        is AddMemoUiState.CreateMode -> {
                            if (!::addMemoAdapter.isInitialized) {
                                initViewPager(content = state.form.content)
                            }
                        }

                        is AddMemoUiState.EditMode -> {
                            if (!::addMemoAdapter.isInitialized) {
                                initViewPager(
                                    content = state.form.content,
                                    page = state.form.page,
                                    backgroundPosition = state.form.backgroundPosition,
                                    tags = state.form.tagList
                                )
                            }
                        }

                        is AddMemoUiState.Success -> {
                            Toast.makeText(this@AddMemoActivity, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                        is AddMemoUiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}