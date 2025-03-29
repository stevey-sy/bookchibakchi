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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivitySearchBookBinding
import com.example.bookchigibakchigi.network.model.AladinBookItem
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addbook.AddBookActivity
import com.example.bookchigibakchigi.ui.searchbook.SearchBookUiState.Loading
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookSearchAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchBookActivity : BaseActivity() {

    private val viewModel: SearchBookViewModel by viewModels()
    private lateinit var binding: ActivitySearchBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addProgressBarToLayout(binding.root)
        setupToolbar(binding.toolbar, binding.main)

        val adapter = BookSearchAdapter { bookItem, sharedView ->
            onBookItemClicked(bookItem, sharedView)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // UiState 관찰
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is SearchBookUiState.Loading -> {
                        showProgressBar()
                    }
                    is SearchBookUiState.Success -> {
                        hideProgressBar()
                        showResults()
                        adapter.submitList(state.books)
                    }
                    is SearchBookUiState.Error -> {
                        hideProgressBar()
                        Toast.makeText(this@SearchBookActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is SearchBookUiState.Empty -> {
                        hideProgressBar()
                        showNoResults()
                    }
                }
            }
        }

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
                true
            } else {
                false
            }
        }

        window.sharedElementReturnTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                binding.recyclerView.itemAnimator = null
            }

            override fun onTransitionEnd(transition: Transition) {
                binding.recyclerView.itemAnimator = DefaultItemAnimator()
            }

            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_book, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onBookItemClicked(bookItem: AladinBookItem, sharedView: View) {
        val intent = Intent(this, AddBookActivity::class.java).apply {
            val itemId = bookItem.isbn13.takeIf { !it.isNullOrEmpty() } ?: bookItem.isbn
            putExtra("itemId", itemId)
            val coverUrl = bookItem.cover.takeIf { !it.isNullOrEmpty() } ?: bookItem.cover
            putExtra("coverUrl", coverUrl)
        }
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            sharedView,
            "shared_element_image"
        )
        startActivity(intent, options.toBundle())
    }

    private fun handleSearch(query: String) {
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