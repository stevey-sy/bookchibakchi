package com.example.bookchigibakchigi.ui.addbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.network.service.AladinBookApiService
import com.example.bookchigibakchigi.data.repository.AladinBookRepository
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.dialog.NotYetReadDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        setupToolbar(binding.toolbar, binding.main)

        // ViewModel 연결
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // 뒤로 가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                animateImageViewSizeAndFinish()
            }
        })

        binding.tvNext.setOnClickListener {
            addBook()
        }

        // Intent에서 데이터 받아오기
        intent.getStringExtra("itemId")?.let { itemId ->
            intent.getStringExtra("coverUrl")?.let { coverUrl ->
                viewModel.getBookItem(itemId, coverUrl)
            }
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is AddBookUiState.Loading -> {
//                            showLoading()
                        }
                        is AddBookUiState.Success -> {
                            showBookDetails(state)
                        }
                        is AddBookUiState.Error -> {
                            showError(state.message)
                        }
                        is AddBookUiState.Initial -> {
                            // 초기 상태 처리
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
    }

    private fun showBookDetails(state: AddBookUiState.Success) {
//        binding.progressBar.visibility = View.GONE
//        binding.contentLayout.visibility = View.VISIBLE
        
        // RatingBar 애니메이션 적용
//        state.book.customerReviewRank?.let { rating ->
//            setRatingWithAnimation(binding.ratingBar, rating / 2.0f)
//        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    private fun addBook() {
        val currentState = viewModel.uiState.value
        if (currentState !is AddBookUiState.Success) {
            Toast.makeText(this, "책 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val book = BookEntity(
            title = currentState.book.title,
            author = currentState.book.author,
            publisher = currentState.book.publisher,
            isbn = currentState.book.isbn,
            coverImageUrl = currentState.coverUrl,
            bookType = "0",
            totalPageCnt = currentState.book.subInfo?.itemPage ?: 0,
            challengePageCnt = 0,
            startDate = "",
            endDate = "",
            currentPageCnt = 0
        )

        if(book.title.isEmpty() || book.author.isEmpty() || book.publisher.isEmpty() || book.isbn.isEmpty()) {
            Toast.makeText(this, "책 저장에 실패했습니다. 잠시 후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@AddBookActivity)
                val bookDao = database.bookDao()
                val isExists = bookDao.isBookExists(book.isbn) > 0
                
                if (isExists) {
                    Toast.makeText(this@AddBookActivity, "이미 저장된 책입니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                bookDao.insertBook(book)
                Toast.makeText(this@AddBookActivity, "나의 서재에 책이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddBookActivity, "책 저장에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBottomSheet() {
        val currentState = viewModel.uiState.value
        if (currentState !is AddBookUiState.Success) {
            Toast.makeText(this, "책 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_add_book_option, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            bottomSheetDialog.dismiss()

            val dialog = NotYetReadDialog(
                context = this,
                fragmentManager = supportFragmentManager,
                pageCnt = currentState.book.subInfo?.itemPage ?: 0,
                onSave = { pagesPerDay, startDate ->
                    val book = BookEntity(
                        title = currentState.book.title,
                        author = currentState.book.author,
                        publisher = currentState.book.publisher,
                        isbn = currentState.book.isbn,
                        coverImageUrl = currentState.coverUrl,
                        bookType = "0",
                        totalPageCnt = currentState.book.subInfo?.itemPage ?: 0,
                        challengePageCnt = pagesPerDay,
                        startDate = startDate,
                        endDate = "",
                        currentPageCnt = pagesPerDay
                    )

                    lifecycleScope.launch {
                        try {
                            val database = AppDatabase.getDatabase(this@AddBookActivity)
                            val bookDao = database.bookDao()
                            bookDao.insertBook(book)
                            Toast.makeText(this@AddBookActivity, "나의 보관소에 책이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@AddBookActivity, "책 저장에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
            dialog.show()
        }

        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun animateImageViewSizeAndFinish() {
        // 원하는 최종 크기
        val targetWidth = resources.getDimensionPixelSize(R.dimen.image_width_small) // 80dp
        val targetHeight = resources.getDimensionPixelSize(R.dimen.image_height_small) // 120dp

        // 현재 크기 가져오기
        val initialWidth = binding.ivBook.width
        val initialHeight = binding.ivBook.height

        // ValueAnimator 설정
        val widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth)
        val heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)

        // 애니메이션 리스너 추가
        widthAnimator.addUpdateListener { animator ->
            val newWidth = animator.animatedValue as Int
            val layoutParams = binding.ivBook.layoutParams
            layoutParams.width = newWidth
            binding.ivBook.layoutParams = layoutParams
        }

        heightAnimator.addUpdateListener { animator ->
            val newHeight = animator.animatedValue as Int
            val layoutParams = binding.ivBook.layoutParams
            layoutParams.height = newHeight
            binding.ivBook.layoutParams = layoutParams
        }

        // 애니메이션 동시 실행
        AnimatorSet().apply {
            playTogether(widthAnimator, heightAnimator)
            duration = 300 // 애니메이션 지속 시간 (ms)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    supportFinishAfterTransition() // 애니메이션 끝난 후 Activity 종료
                }
            })
            start()
        }
    }

    fun setRatingWithAnimation(ratingBar: RatingBar, rating: Float) {
        val animator = ValueAnimator.ofFloat(0f, rating).apply {
            duration = 1000 // 애니메이션 지속 시간
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                ratingBar.rating = animatedValue
            }
        }
        animator.start()
    }
}