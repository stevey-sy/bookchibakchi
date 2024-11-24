package com.example.bookchigibakchigi.ui.searchbook

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivitySearchBookBinding
import com.example.bookchigibakchigi.network.model.BookItem
import com.example.bookchigibakchigi.network.service.NaverBookService
import com.example.bookchigibakchigi.repository.BookSearchRepository
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addbook.AddBookActivity
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookSearchAdapter
import com.example.bookchigibakchigi.viewmodel.BookViewModel
import com.example.bookchigibakchigi.viewmodel.BookViewModelFactory

class SearchBookActivity : BaseActivity() {

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory(BookSearchRepository(NaverBookService.create()))
    }

    private lateinit var binding: ActivitySearchBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 데이터 바인딩 객체 초기화
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addProgressBarToLayout(binding.root);

        // Toolbar를 ActionBar로 설정
        setupToolbar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView 설정
        val adapter = BookSearchAdapter{ bookItem, sharedView->
            onBookItemClicked(bookItem, sharedView)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // ViewModel의 LiveData 관찰
        bookViewModel.bookSearchResults.observe(this, Observer { response ->
            response?.let {
                adapter.submitList(it)
            }
        })

        // 로딩 상태 관찰
        bookViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) showProgressBar() else hideProgressBar()
        }

        bookViewModel.errorMessage.observe(this, Observer { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                showProgressBar() // ProgressBar 표시
                performSearch(query)
            } else {
                Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        window.sharedElementReturnTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                binding.recyclerView.itemAnimator = null // RecyclerView 애니메이터 비활성화
            }

            override fun onTransitionEnd(transition: Transition) {
                binding.recyclerView.itemAnimator = DefaultItemAnimator() // 애니메이터 복원
            }

            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
        })
    }

    // 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_book, menu) // 메뉴 파일 연결
        return true
    }

    // 메뉴 아이템 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                finish() // 닫기 버튼 클릭 시 Activity 종료
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onBookItemClicked(bookItem: BookItem, sharedView: View) {
        // 책 추가 화면으로 이동
        val intent = Intent(this, AddBookActivity::class.java).apply {
            putExtra("bookItem", bookItem) // Book 객체 전달
        }
//        // 트랜지션 애니메이션 설정
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            sharedView, // 공유 요소 뷰 (예: 이미지)
            "shared_element_image" // transitionName과 일치해야 함
        )
        startActivity(intent, options.toBundle())
    }

//    private fun showProgressBar() {
//        binding.progressBar.visibility = View.VISIBLE
//    }
//
//    private fun hideProgressBar() {
//        binding.progressBar.visibility = View.GONE
//    }

    private fun performSearch(query: String) {
        bookViewModel.searchBooks(query)
    }
}