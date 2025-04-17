package com.example.bookchigibakchigi.ui.mylibrary

import android.graphics.Rect
import android.os.Bundle
import android.transition.TransitionInflater
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

@AndroidEntryPoint
class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BookShelfAdapter
    private val mainViewModel: MainViewModel by activityViewModels()
    private var lastClickedSharedView: View? = null
    private var navigationJob: Job? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_book_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    // 선택된 아이템 삭제 로직
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.setSelectionMode(false)
            actionMode = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        prepareTransitions()
        postponeEnterTransition()
        return root
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshShelf()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
        initRecyclerView()
        initClickListeners()
        observeViewModel()
        observeNavigationEvents()
    }

    private fun initBinding() {
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun initRecyclerView() {
        adapter = BookShelfAdapter(
            onItemClick = { bookEntity, position, sharedView ->
                if (adapter.isSelectionMode()) {
                    adapter.toggleItemSelection(position)
                    updateActionModeTitle()
                } else {
                    lastClickedSharedView = sharedView
                    mainViewModel.navigateToBookDetail(bookEntity, position, sharedView)
                }
            },
            onItemLongClick = { bookEntity ->
                if (!adapter.isSelectionMode()) {
                    adapter.setSelectionMode(true)
                    actionMode = requireActivity().startActionMode(actionModeCallback)
                    updateActionModeTitle()
                }
            }
        )
        binding.rvShelf.layoutManager = GridLayoutManager(context, 3)
        binding.rvShelf.adapter = adapter

        binding.rvShelf.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, 0, 0)
            }
        })
    }

    private fun initClickListeners() {
        binding.btnSearchBook.setOnClickListener {
            findNavController().navigate(R.id.navigation_search_book)
        }

        // 뒤로가기 버튼 처리
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    adapter.setSelectionMode(false)
                    actionMode?.finish()
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
                            if (state.books.isEmpty()) {
                                showEmptyState()
                            } else {
                                showBookList(state.books)
                            }
                        }
                        is MainViewUiState.Empty -> {
                            showEmptyState()
                        }
                        else -> {
                            // 다른 상태는 무시
                        }
                    }
                }
            }
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
                                Log.d("observeNavigationEvents", event.toString())
                                val bundle = Bundle().apply {
                                    putInt("itemId", event.book.itemId)
                                    putString("transitionName", event.transitionName)
                                }

                                if(lastClickedSharedView != null) {
                                    findNavController().navigate(
                                        R.id.action_myLibrary_to_bookDetail,
                                        bundle,
                                        null,
                                        FragmentNavigatorExtras((lastClickedSharedView to event.transitionName) as Pair<View, String>)
                                    )
                                }
                            }
                        }
                    }
            }
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
            rvShelf.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        rvShelf.viewTreeObserver.removeOnPreDrawListener(this)
                        startPostponedEnterTransition()
                        return true
                    }
                }
            )
        }
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
                if(selectedViewHolder == null) {
                    return
                }
                sharedElements[names[0]] =
                    selectedViewHolder.itemView.findViewById(R.id.cardView)
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

                if (viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)) {
                    binding.rvShelf.post {
                        layoutManager?.scrollToPosition(position)
                    }
                }
            }
        })
    }

    private fun updateActionModeTitle() {
        val selectedCount = adapter.getSelectedItems().size
        actionMode?.title = "$selectedCount selected"
    }

    override fun onDestroyView() {
        navigationJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}