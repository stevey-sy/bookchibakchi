package com.example.bookchigibakchigi.ui.searchbook

import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ActivitySearchBookBinding
import com.example.bookchigibakchigi.model.SearchBookUiModel
import com.example.bookchigibakchigi.ui.BaseActivity
import com.example.bookchigibakchigi.ui.addbook.AddBookActivity
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookPagingAdapter
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookSearchAdapter
import com.example.bookchigibakchigi.ui.searchbook.adapter.ShimmerAdapter
import com.example.bookchigibakchigi.util.KeyboardUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchBookActivity : BaseActivity() {
    private val viewModel: SearchBookViewModel by viewModels()
    private lateinit var binding: ActivitySearchBookBinding
    private lateinit var adapter: BookPagingAdapter
    private lateinit var shimmerAdapter: ShimmerAdapter

    private var loadingStartTime: Long = 0
    private val MIN_SHIMMER_DURATION = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initBinding()
        initToolbar()
        initView()
        initClickListeners()
        initTransitionAnimation()
        observeUiState()
    }

    private fun initBinding() {
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun initToolbar() {
        initToolbar(binding.toolbar, binding.main)
    }

    private fun initView() {
        adapter = BookPagingAdapter(::onBookItemClicked)
        shimmerAdapter = ShimmerAdapter()
        binding.recyclerView.adapter = shimmerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this@SearchBookActivity)

        // 결과 감지
        adapter.addLoadStateListener { loadState ->
            val isLoaded = loadState.source.refresh is LoadState.NotLoading
            val isEmpty = adapter.itemCount == 0
            if (isLoaded && isEmpty) {
                viewModel.setNoResult()
            }
        }
    }

    private fun initClickListeners() {
        binding.flSearchBtn.setOnClickListener {
            KeyboardUtil.hideKeyboard(this, binding.searchEditText)
            Log.d("initClickListeners", "initClickListeners: ")

            lifecycleScope.launch {
                viewModel.searchBooks(binding.searchEditText.text.toString()).collectLatest { pagingData ->
                    adapter.submitDataWithAnimation(pagingData)
                    KeyboardUtil.hideKeyboard(this@SearchBookActivity, binding.searchEditText)
                }
            }
        }

        binding.searchEditText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
//                viewModel.onSearchAction(textView.text.toString())
//                KeyboardUtil.hideKeyboard(this, binding.searchEditText)
                lifecycleScope.launch {
                    viewModel.searchBooks(binding.searchEditText.text.toString()).collectLatest { pagingData ->
                        adapter.submitDataWithAnimation(pagingData)
                        KeyboardUtil.hideKeyboard(this@SearchBookActivity, binding.searchEditText)
                    }
                }
                true
            } else {
                false
            }
        }
    }

    private fun initTransitionAnimation() {
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

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                var progressBarVisibility = View.GONE
                when (state) {
                    is SearchBookUiState.Success -> {
                        val elapsed = System.currentTimeMillis() - loadingStartTime
                        if (elapsed < MIN_SHIMMER_DURATION) {
                            delay(MIN_SHIMMER_DURATION - elapsed)
                        }
                        binding.recyclerView.adapter = adapter
                        // PagingData는 이미 adapter.submitData()로 처리되므로 여기서는 추가 작업이 필요 없습니다
                    }
                    is SearchBookUiState.Error -> {
                        Toast.makeText(this@SearchBookActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is SearchBookUiState.Loading -> {
                        // 로딩 상태 처리 (필요한 경우 로딩 인디케이터 표시)
                        progressBarVisibility= View.VISIBLE
                        loadingStartTime = System.currentTimeMillis()
                        binding.recyclerView.adapter = shimmerAdapter
                    }
                    else -> {

                    }
                }

                binding.progressBar.visibility = progressBarVisibility
            }
        }
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

    private fun onBookItemClicked(bookItem: SearchBookUiModel, sharedView: View) {
        val intent = Intent(this, AddBookActivity::class.java).apply {
            val itemId = bookItem.isbn.takeIf { !it.isNullOrEmpty() } ?: bookItem.isbn
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
}