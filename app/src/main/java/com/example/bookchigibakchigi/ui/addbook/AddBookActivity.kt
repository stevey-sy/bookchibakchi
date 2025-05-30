package com.example.bookchigibakchigi.ui.addbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddBookActivity : BaseActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private val viewModel: AddBookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar(binding.toolbar, binding.main)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initBackPressCallback()
        initBookDataFromIntent()
        observeViewModel()
    }

    private fun initBackPressCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                animateImageViewSizeAndFinish()
            }
        })
    }

    private fun initBookDataFromIntent() {
        intent.getStringExtra("itemId")?.let { itemId ->
            intent.getStringExtra("coverUrl")?.let { coverUrl ->
                viewModel.getBookItem(itemId, coverUrl)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is AddBookUiState.Initial -> { /* 초기 상태 처리 */ }
                        is AddBookUiState.Loading -> { /* 로딩 상태 처리 */ }
                        is AddBookUiState.Success -> {
                            if (state.isSaved) {
                                state.savedItemId?.let { itemId ->
                                    binding.ivBook.transitionName = "sharedView_$itemId"
                                }
                                Toast.makeText(this@AddBookActivity, "나의 서재에 책이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                navigateToMainActivity()
                            }
                        }
                        is AddBookUiState.Error -> {
                            Toast.makeText(this@AddBookActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    private fun animateImageViewSizeAndFinish() {
        val targetWidth = resources.getDimensionPixelSize(R.dimen.image_width_small)
        val targetHeight = resources.getDimensionPixelSize(R.dimen.image_height_small)

        val initialWidth = binding.ivBook.width
        val initialHeight = binding.ivBook.height

        val widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth)
        val heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)

        widthAnimator.addUpdateListener { animator ->
            val newWidth = animator.animatedValue as Int
            binding.ivBook.layoutParams = binding.ivBook.layoutParams.apply { width = newWidth }
        }

        heightAnimator.addUpdateListener { animator ->
            val newHeight = animator.animatedValue as Int
            binding.ivBook.layoutParams = binding.ivBook.layoutParams.apply { height = newHeight }
        }

        AnimatorSet().apply {
            playTogether(widthAnimator, heightAnimator)
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    supportFinishAfterTransition()
                }
            })
            start()
        }
    }
}