package com.example.bookchigibakchigi.ui.addbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.network.model.BookItem
import com.example.bookchigibakchigi.network.service.AladinBookApiService
import com.example.bookchigibakchigi.repository.AladinBookRepository
import com.example.bookchigibakchigi.ui.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar
import java.util.TimeZone

class AddBookActivity : BaseActivity() {

    private lateinit var binding: ActivityAddBookBinding

    private val viewModel: AddBookActivityViewModel by viewModels {
        // Intent에서 BookItem? 데이터를 추출
        val itemId = intent.getStringExtra("itemId") ?: throw IllegalArgumentException("itemId가 필요합니다.")
        val apiService = AladinBookApiService.create()
        val repository = AladinBookRepository(apiService)
        AddBookActivityViewModelFactory(itemId, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbar, binding.main)

        // ViewModel 연결
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // 뒤로 가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                animateImageViewSizeAndFinish() // 커스텀 애니메이션 호출
            }
        })

        binding.tvNext.setOnClickListener {
            showBottomSheet()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_book, menu) // 메뉴 파일 연결
        return true
    }

    // 메뉴 아이템 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                animateImageViewSizeAndFinish()
//                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_add_book_option, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
        // 추가 로직 (예: 버튼 클릭 이벤트)
        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            // 버튼 클릭 동작
            bottomSheetDialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            // 버튼 클릭 동작
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