package com.example.bookchigibakchigi.ui.record

import android.animation.Animator
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityRecordBinding
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.dialog.PageInputDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordActivity : BaseActivity() {

    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowTransitions()
        enableEdgeToEdge()
        setupBinding()
        setupBookFromIntent()
        setupWindowInsets()
        setupClickListeners()
        observeStateFlows()
    }

    private fun setupWindowTransitions() {
        window.sharedElementEnterTransition = createSharedElementTransition()
        window.sharedElementReturnTransition = createSharedElementTransition()
    }

    private fun setupBinding() {
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportPostponeEnterTransition()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun setupBookFromIntent() {
        val book: BookEntity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("currentBook", BookEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("currentBook")
        }

        book?.let {
            viewModel.setCurrentBook(it)
            setupBookCoverTransition(it)
        }
    }

    private fun setupBookCoverTransition(book: BookEntity) {
        binding.ivBookCover.transitionName = "record_act_shared_view_${book.itemId}"
        binding.ivBookCover.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.ivBookCover.viewTreeObserver.removeOnPreDrawListener(this)
                supportStartPostponedEnterTransition()
                return true
            }
        })
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
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
//        binding.btnOut.visibility = when (state) {
//            is RecordUiState.Completed -> View.VISIBLE
//            else -> View.GONE
//        }

        if (state is RecordUiState.Completed) {
            playCompletionAnimation()
        }
    }

    private fun playCompletionAnimation() {
        binding.animView.setAnimation(R.raw.anim_congrats)
        binding.animView.playAnimation()

        binding.animViewComplete.setAnimation(R.raw.anim_complete)
        binding.animViewComplete.playAnimation()

        binding.animViewComplete.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                binding.animViewComplete.progress = 0.8F
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun showPageInputDialog() {
        viewModel.currentBook.value?.let { book ->
            PageInputDialog(
                context = this,
                currentBook = book,
                onComplete = { page ->
                    lifecycleScope.launch {
                        val success = viewModel.updateReadingProgress(page)
                        if (success) {
                            // 상태를 유지하기 위해 finishWithTransition() 대신 finish() 사용
                            finish()
//                            finishWithTransition()
                        } else {
                            // 데이터베이스 업데이트가 실패한 경우 처리
                            // 예: 토스트 메시지 표시
                        }
                    }
                },
                onAllComplete = {
                    lifecycleScope.launch {
                        viewModel.completeReading()
                        delay(500)
                        finishWithTransition()
                    }
                }
            ).show()
        }
    }

    private fun createSharedElementTransition(): Transition {
        return TransitionInflater.from(this)
            .inflateTransition(R.transition.grid_to_pager_transition).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }
    }
    
    private fun finishWithTransition() {
        ActivityCompat.finishAfterTransition(this)
    }
}