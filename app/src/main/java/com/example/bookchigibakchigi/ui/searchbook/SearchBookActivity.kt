package com.example.bookchigibakchigi.ui.searchbook

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import com.example.bookchigibakchigi.repository.BookRepository
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookAdapter
import com.example.bookchigibakchigi.viewmodel.BookViewModel
import com.example.bookchigibakchigi.viewmodel.BookViewModelFactory

class SearchBookActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory(BookRepository(NaverBookService.create()))
    }

    private lateinit var binding: ActivitySearchBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 데이터 바인딩 객체 초기화
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView 설정
        val adapter = BookAdapter()
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

        // 검색 버튼 클릭 리스너 설정
        val searchButton = findViewById<Button>(R.id.searchButton)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                bookViewModel.searchBooks(query)
            } else {
                Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}