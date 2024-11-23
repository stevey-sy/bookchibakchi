package com.example.bookchigibakchigi.ui.addbook

import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
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

        // 공유 요소 트랜지션 설정
        // 공유 요소 트랜지션 활성화
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.shared_element_transition)

        window.sharedElementReturnTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.shared_element_transition)

        viewModel.bookItem.observe(this) { bookItem ->
            bookItem?.let {
                binding.tvBookTitle.text = it.title
                binding.tvAuthor.text = it.author
                binding.tvPublisher.text = it.publisher
                binding.tvIsbn.text = it.isbn
                Glide.with(this).load(it.image).into(binding.ivBook)
            }
        }

        window.sharedElementReturnTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                // 트랜지션 시작 시, ScaleType을 동일하게 설정
                binding.ivBook.scaleType = ImageView.ScaleType.CENTER_CROP
            }

            override fun onTransitionEnd(transition: Transition) {
                // 트랜지션 종료 후, 원래 ScaleType 복원
                binding.ivBook.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
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
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}