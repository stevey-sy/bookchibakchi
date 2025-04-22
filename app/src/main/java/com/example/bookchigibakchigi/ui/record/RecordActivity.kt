package com.example.bookchigibakchigi.ui.record

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityRecordBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.dialog.PageInputDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordActivity : BaseActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initBookFromIntent()
        initClickListeners()
        observeStateFlows()
    }

    private fun initBinding() {
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportPostponeEnterTransition()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun initBookFromIntent() {
        val bookId: Int? = intent.getIntExtra("bookId", -1)
        bookId?.let {
            viewModel.setSelectedBook(it)
        }
    }

    private fun initClickListeners() {
        binding.btnPause.setOnClickListener { viewModel.toggleTimer() }
        binding.btnClose.setOnClickListener { finishWithTransition() }
        binding.btnComplete.setOnClickListener {
            viewModel.pauseReading()
            showPageInputDialog()
        }
        binding.btnOut.setOnClickListener { finishWithTransition() }
    }

    private fun observeStateFlows() {
        lifecycleScope.launch {
            launch { viewModel.timerText.collectLatest { binding.tvTimer.text = it } }
            launch { viewModel.uiState.collectLatest { state ->
                updateUiState(state)
            }}
        }
    }

    private fun updateUiState(state: RecordUiState) {
        binding.btnOut.visibility = when (state) {
            is RecordUiState.Completed -> View.VISIBLE
            else -> View.GONE
        }

        binding.btnComplete.visibility = when (state) {
            is RecordUiState.Completed -> View.GONE
            else -> View.VISIBLE
        }

        if (state is RecordUiState.Completed) {
            playCompletionAnimation()
        }
    }

    private fun playCompletionAnimation() {
        binding.animView.setAnimation(R.raw.anim_congrats)
        binding.animView.playAnimation()

        binding.animViewComplete.setAnimation(R.raw.anim_complete)
        binding.animViewComplete.setMinAndMaxProgress(0.0f, 0.8f)
        binding.animViewComplete.playAnimation()

        binding.animViewComplete.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
//                binding.animViewComplete.progress = 0.8F
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun showPageInputDialog() {
        viewModel.selectedBook.value?.let { book ->
            PageInputDialog(
                context = this,
                currentBook = book,
                onComplete = { page ->
                    lifecycleScope.launch {
                        val success = viewModel.updateReadingProgress(page)
                        if (!success) {
                            // 데이터베이스 업데이트가 실패한 경우 처리
                            // 예: 토스트 메시지 표시
                        } else {
                            finish()
                        }
                    }
                },
                onAllComplete = {
                    lifecycleScope.launch {
                        viewModel.completeReading()
                    }
                }
            ).show()
        }
    }
    
    private fun finishWithTransition() {
        ActivityCompat.finishAfterTransition(this)
    }
}