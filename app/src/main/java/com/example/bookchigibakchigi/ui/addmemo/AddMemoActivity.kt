package com.example.bookchigibakchigi.ui.addmemo

import android.app.Dialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.widget.doAfterTextChanged
import com.example.bookchigibakchigi.databinding.ActivityAddMemoBinding
import com.example.bookchigibakchigi.databinding.DialogColorPickerBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addmemo.adapter.TagListAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddMemoActivity : BaseActivity() {

    private lateinit var binding: ActivityAddMemoBinding
    private val viewModel: AddMemoViewModel by viewModels()
    private var pageDebounceJob: Job? = null
    private var contentDebounceJob: Job? = null
    private lateinit var tagListAdapter: TagListAdapter
    private var colorPickerDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initRecyclerView()
        
        // copiedText 처리
        val copiedText = intent.getStringExtra("copiedText")
        if (!copiedText.isNullOrEmpty()) {
            binding.etContent.setText(copiedText)
            viewModel.onEvent(AddMemoEvent.UpdateContent(copiedText))
        }
        
        initListeners()
        observeViewModel()
    }

    private fun initRecyclerView() {
        tagListAdapter = TagListAdapter()

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        binding.rvTagList.apply {
            layoutManager = flexboxLayoutManager
            adapter = tagListAdapter
        }
    }

    private fun initListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.etPage.doAfterTextChanged { text ->
            pageDebounceJob?.cancel()
            pageDebounceJob = lifecycleScope.launch {
                delay(300)
                viewModel.onEvent(AddMemoEvent.UpdatePage(text.toString()))
            }
        }

        binding.etContent.doAfterTextChanged { text ->
            contentDebounceJob?.cancel()
            contentDebounceJob = lifecycleScope.launch {
                delay(300)
                viewModel.onEvent(AddMemoEvent.UpdateContent(text.toString()))
            }
        }

        binding.vColorSelector.setOnClickListener {
            showColorPickerDialog()
        }

        binding.btnAddTag.setOnClickListener {
            val tagName = binding.etTag.text.toString().trim()
            if (tagName.isNotEmpty()) {
                viewModel.onEvent(AddMemoEvent.AddTag(tagName, "#FF0000", "#FFFFFF"))
                binding.etTag.text.clear()
            }
        }

        binding.saveButton.setOnClickListener {
            viewModel.onEvent(AddMemoEvent.SaveMemo)
        }
    }

    private fun showColorPickerDialog() {
        colorPickerDialog?.dismiss()
        val dialogBinding = DialogColorPickerBinding.inflate(layoutInflater)
        colorPickerDialog = Dialog(this).apply {
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(true)
            
            // 다이얼로그 크기 설정
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.8).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        with(dialogBinding) {
            colorGray.setOnClickListener { selectColor("@color/gray") }
            colorRed.setOnClickListener { selectColor("#FF0000") }
            colorOrange.setOnClickListener { selectColor("#FFA500") }
            colorGreen.setOnClickListener { selectColor("#00FF00") }
            colorBlue.setOnClickListener { selectColor("#0000FF") }
            colorPurple.setOnClickListener { selectColor("#800080") }
            colorPink.setOnClickListener { selectColor("#FFC0CB") }
        }

        colorPickerDialog?.show()
    }

    private fun selectColor(color: String) {
        binding.vColorSelector.backgroundTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(color)
        )
        colorPickerDialog?.dismiss()
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
        if (state.isSuccess) {
            finish()
        }

        tagListAdapter.submitList(state.tagList)
        // 태그 리스트가 업데이트된 후 최하단으로 스크롤
        binding.rvTagList.post {
            lifecycleScope.launch {
                delay(300) // 필요에 따라 조절
                val scrollAmount = binding.scrollView.getChildAt(0).height
                binding.scrollView.smoothScrollTo(0, scrollAmount)
            }
        }

        state.error?.let { error ->
            showError(error)
        }
    }

    private fun showError(message: String) {
        // 에러 메시지를 표시하는 로직 구현
    }

    override fun onDestroy() {
        super.onDestroy()
        colorPickerDialog?.dismiss()
    }
}