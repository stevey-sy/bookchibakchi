package com.example.bookchigibakchigi.ui.searchbook

import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivitySearchBookBinding
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.network.service.AladinBookApiService
import com.example.bookchigibakchigi.data.repository.AladinBookRepository
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addbook.AddBookActivity
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookSearchAdapter

class SearchBookActivity : BaseActivity() {

    private val viewModel: SearchBookActivityViewModel by viewModels {
        SearchBookActivityViewModelFactory(AladinBookRepository(AladinBookApiService.create()))
    }

    private lateinit var binding: ActivitySearchBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 데이터 바인딩 객체 초기화
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addProgressBarToLayout(binding.root);
        setupToolbar(binding.toolbar, binding.main)

        // RecyclerView 설정
        val adapter = BookSearchAdapter{ bookItem, sharedView->
            onBookItemClicked(bookItem, sharedView)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // ViewModel의 LiveData 관찰
        viewModel.bookSearchResults.observe(this, Observer { response ->
            response?.let {
                if (it.isEmpty()) {
                    showNoResults()
                } else {
                    showResults()
                    adapter.submitList(it)
                }
            }
        })

        // 로딩 상태 관찰
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) showProgressBar() else hideProgressBar()
        }

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                handleSearch(query)
            } else {
                Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.searchEditText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = textView.text.toString()
                if (query.isNotBlank()) {
                    handleSearch(query)
                }
                true // 이벤트 처리 완료
            } else {
                false // 이벤트 처리되지 않음
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

    private fun onBookItemClicked(bookItem: AladinBookItem, sharedView: View) {
        // 책 추가 화면으로 이동
        val intent = Intent(this, AddBookActivity::class.java).apply {
            val itemId = bookItem.isbn13.takeIf { !it.isNullOrEmpty() } ?: bookItem.isbn
            putExtra("itemId", itemId) // Book 객체 전달
            val coverUrl = bookItem.cover.takeIf { !it.isNullOrEmpty() } ?: bookItem.cover
            putExtra("coverUrl", coverUrl)
        }
//        // 트랜지션 애니메이션 설정
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            sharedView, // 공유 요소 뷰 (예: 이미지)
            "shared_element_image" // transitionName과 일치해야 함
        )
        startActivity(intent, options.toBundle())
    }

    private fun handleSearch(query: String) {
        showProgressBar() // ProgressBar 표시
        viewModel.searchBooks(query)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun showNoResults() {
        binding.recyclerView.visibility = View.GONE
        binding.noResultsLayout.visibility = View.VISIBLE
    }

    private fun showResults() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.noResultsLayout.visibility = View.GONE
    }


}