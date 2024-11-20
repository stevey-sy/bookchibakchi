package com.example.bookchigibakchigi.ui.searchbook

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivitySearchBookBinding
import com.example.bookchigibakchigi.network.service.NaverBookService
import com.example.bookchigibakchigi.repository.BookSearchRepository
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookSearchAdapter
import com.example.bookchigibakchigi.viewmodel.BookViewModel
import com.example.bookchigibakchigi.viewmodel.BookViewModelFactory

class SearchBookActivity : AppCompatActivity() {

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

        // RecyclerView 설정
        val adapter = BookSearchAdapter{ position ->
            onBookItemClicked(position)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // ViewModel의 LiveData 관찰
        bookViewModel.bookSearchResults.observe(this, Observer { response ->
            response?.let {
                adapter.submitList(it.items)
            }
        })

        bookViewModel.errorMessage.observe(this, Observer { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                bookViewModel.searchBooks(query)
            } else {
                Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun onBookItemClicked(position: Int) {
        // 책 추가 dialog

    }
}