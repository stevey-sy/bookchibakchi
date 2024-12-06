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
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivityAddBookBinding
import com.example.bookchigibakchigi.network.model.BookItem

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding

    private val viewModel: AddBookActivityViewModel by viewModels {
        // Intent에서 BookItem? 데이터를 추출
        val bookItem: BookItem? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("bookItem", BookItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("bookItem")
        }

        // ViewModelFactory에 전달
        AddBookActivityViewModelFactory(bookItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar를 ActionBar로 설정
        setSupportActionBar(binding.toolbar)
        // 백버튼 제거
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.bookItem.observe(this) { bookItem ->
            bookItem?.let {
                binding.tvBookTitle.text = it.title
                binding.tvAuthor.text = it.author
                binding.tvPublisher.text = it.publisher
                binding.tvIsbn.text = it.isbn
                Glide.with(this).load(it.image).into(binding.ivBook)
            }
        }

        // 뒤로 가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                animateImageViewSizeAndFinish() // 커스텀 애니메이션 호출
            }
        })
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


}