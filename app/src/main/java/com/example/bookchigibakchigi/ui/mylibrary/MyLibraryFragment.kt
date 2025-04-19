package com.example.bookchigibakchigi.ui.mylibrary

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding
import com.example.bookchigibakchigi.ui.main.MainViewModel
import com.example.bookchigibakchigi.ui.main.MainViewUiState
import com.example.bookchigibakchigi.ui.main.NavigationEvent
import com.example.bookchigibakchigi.ui.mylibrary.adapter.BookShelfAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Job
import androidx.activity.OnBackPressedCallback
import android.view.ActionMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookchigibakchigi.ui.common.SelectionActionMode

@AndroidEntryPoint
class MyLibraryFragment : Fragment() {

    companion object {
        private const val GRID_SPAN_COUNT = 3
    }

    private var _binding: FragmentMyLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BookShelfAdapter
    private val mainViewModel: MainViewModel by activityViewModels()
    private var lastClickedSharedView: View? = null
    private var navigationJob: Job? = null
    private lateinit var selectionActionMode: SelectionActionMode

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        prepareTransitions()
        postponeEnterTransition()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSelectionActionMode()
        initViews()
        initObservers()
    }

    private fun initSelectionActionMode() {
        selectionActionMode = SelectionActionMode(
            activity = requireActivity() as AppCompatActivity,
            onDelete = { handleDeleteAction() },
            onSelectionModeChanged = { isSelectionMode ->
                adapter.setSelectionMode(isSelectionMode)
            }
        )
    }

    private fun initViews() {
        initBinding()
        initRecyclerView()
        initClickListeners()
    }

    private fun initObservers() {
        observeViewModel()
        observeNavigationEvents()
    }

    private fun initBinding() {
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun initRecyclerView() {
        adapter = createBookShelfAdapter()
        binding.rvShelf.apply {
            layoutManager = GridLayoutManager(context, GRID_SPAN_COUNT)
            adapter = this@MyLibraryFragment.adapter
            addItemDecoration(createItemDecoration())
        }
    }

    private fun createBookShelfAdapter() = BookShelfAdapter(
        onItemClick = { bookEntity, position, sharedView ->
            handleItemClick(bookEntity, position, sharedView)
        },
        onItemLongClick = { bookEntity ->
            handleItemLongClick()
        }
    )

    private fun createItemDecoration() = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(0, 0, 0, 0)
        }
    }

    private fun handleItemClick(bookEntity: BookEntity, position: Int, sharedView: View) {
        if (adapter.isSelectionMode()) {
            adapter.toggleItemSelection(position)
            selectionActionMode.updateSelectedCount(adapter.getSelectedItems().size)
        } else {
            lastClickedSharedView = sharedView
            mainViewModel.navigateToBookDetail(bookEntity, position, sharedView)
        }
    }

    private fun handleItemLongClick() {
        if (!adapter.isSelectionMode()) {
            selectionActionMode.start(binding.rvShelf)
            selectionActionMode.updateSelectedCount(1)
        }
    }

    private fun handleDeleteAction() {
        val selectedBooks = adapter.getSelectedItems()
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.deleteSelectedBooks(selectedBooks)
            selectionActionMode.finish()
            Toast.makeText(requireContext(), "${selectedBooks.size}개의 책이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initClickListeners() {
        binding.btnSearchBook.setOnClickListener {
            findNavController().navigate(R.id.navigation_search_book)
        }
        setupBackPressedCallback()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    selectionActionMode.finish()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collectLatest { state ->
                    when (state) {
                        is MainViewUiState.MyLibrary -> {
                            handleLibraryState(state.books)
                        }
                        is MainViewUiState.Empty -> {
                            showEmptyState()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun handleLibraryState(books: List<BookEntity>) {
        if (books.isEmpty()) {
            showEmptyState()
        } else {
            showBookList(books)
        }
    }

    private fun observeNavigationEvents() {
        navigationJob?.cancel()
        
        navigationJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.navigationEventFlow
                    .distinctUntilChanged()
                    .collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateToBookDetail -> {
                                handleNavigationToBookDetail(event)
                            }
                        }
                    }
            }
        }
    }

    private fun handleNavigationToBookDetail(event: NavigationEvent.NavigateToBookDetail) {
        Log.d("observeNavigationEvents", event.toString())
        val bundle = Bundle().apply {
            putInt("itemId", event.book.itemId)
            putString("transitionName", event.transitionName)
        }

        lastClickedSharedView?.let { sharedView ->
            findNavController().navigate(
                R.id.action_myLibrary_to_bookDetail,
                bundle,
                null,
                FragmentNavigatorExtras(sharedView to event.transitionName)
            )
        }
    }

    private fun showEmptyState() {
        binding.apply {
            rvShelf.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }
    }

    private fun showBookList(books: List<BookEntity>) {
        binding.apply {
            rvShelf.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            adapter.setDataList(books)
            initTransitionListener()
        }
    }

    private fun initTransitionListener() {
        binding.rvShelf.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.rvShelf.viewTreeObserver.removeOnPreDrawListener(this)
                    startPostponedEnterTransition()
                    return true
                }
            }
        )
    }

    private fun prepareTransitions() {
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
                val currentTransitionName = findNavController()
                    .currentBackStackEntry?.savedStateHandle?.get<String>("current_transition_name")
                val currentPosition = findNavController()
                    .currentBackStackEntry?.savedStateHandle?.get<Int>("selected_position") ?: -1

                if (currentTransitionName.isNullOrEmpty() || currentPosition == -1) return
                scrollToPosition(currentPosition)

                val selectedViewHolder = binding.rvShelf.findViewHolderForAdapterPosition(currentPosition)
                selectedViewHolder?.let {
                    sharedElements[names[0]] = it.itemView.findViewById(R.id.cardView)
                }
            }
        })
    }

    private fun scrollToPosition(position: Int) {
        binding.rvShelf.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                binding.rvShelf.removeOnLayoutChangeListener(this)
                val layoutManager = binding.rvShelf.layoutManager
                val viewAtPosition = layoutManager?.findViewByPosition(position)

                if (viewAtPosition == null || layoutManager?.isViewPartiallyVisible(viewAtPosition, false, true) == true) {
                    binding.rvShelf.post {
                        layoutManager?.scrollToPosition(position)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        navigationJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}