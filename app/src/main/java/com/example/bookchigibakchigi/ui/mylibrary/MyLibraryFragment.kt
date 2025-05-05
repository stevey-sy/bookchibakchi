package com.example.bookchigibakchigi.ui.mylibrary

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding
import com.example.bookchigibakchigi.ui.main.MainViewModel
import com.example.bookchigibakchigi.ui.main.MainViewUiState
import com.example.bookchigibakchigi.ui.main.NavigationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Job
import androidx.activity.OnBackPressedCallback
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.ui.common.SelectionActionMode
import com.example.bookchigibakchigi.ui.dialog.TwoButtonsDialog
import com.example.bookchigibakchigi.ui.main.MainActivity
import com.example.bookchigibakchigi.ui.mylibrary.adapter.BookListAdapter
import com.example.bookchigibakchigi.ui.searchbook.SearchBookActivity

@AndroidEntryPoint
class MyLibraryFragment : Fragment() {
    private var _binding: FragmentMyLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var listAdapter: BookListAdapter
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

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.apply {
            updateToolbarTitle(
                title = "오독오독",
                fontResId = R.font.dashi,
                textSizeSp = 30f,
                menuResId = R.menu.menu_my_library
            )
            // invalidateOptionsMenu() // ✅ 여기에서 직접 호출 OK
        }
    }

    private fun initSelectionActionMode() {
        selectionActionMode = SelectionActionMode(
            activity = requireActivity() as AppCompatActivity,
            onDelete = { handleDeleteAction() },
            onSelectionModeChanged = { isSelectionMode ->
                listAdapter.setSelectionMode(isSelectionMode)
            }
        )
    }

    private fun initViews() {
        initBinding()
        initListView()
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

    private fun initListView() {
        listAdapter = BookListAdapter(
            onItemClick = { bookUiModel, position, sharedView ->
                handleItemClick(bookUiModel, position, sharedView)
            },
            onItemLongClick = { bookUiModel ->
                handleItemLongClick()
            }
        )
        binding.rvList.apply {
            adapter = listAdapter
            layoutManager = GridLayoutManager(context, 3)
            setHasFixedSize(true)
        }
    }

    private fun handleItemClick(bookUiModel: BookUiModel, position: Int, sharedView: View) {
        if (listAdapter.isSelectionMode()) {
            listAdapter.toggleItemSelection(position)
            selectionActionMode.updateSelectedCount(listAdapter.getSelectedItems().size)
        } else {
            lastClickedSharedView = sharedView
            mainViewModel.navigateToBookDetail(bookUiModel, position, sharedView)
        }
    }

    private fun handleItemLongClick() {
        if (!listAdapter.isSelectionMode()) {
            selectionActionMode.start(binding.rvList)
            selectionActionMode.updateSelectedCount(1)
        }
    }

    private fun handleDeleteAction() {
        val selectedBooks = listAdapter.getSelectedItems()
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.deleteSelectedBooks(selectedBooks)
            selectionActionMode.finish()
            Toast.makeText(requireContext(), "${selectedBooks.size}개의 책이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initClickListeners() {
        binding.btnSearchBook.setOnClickListener {
            val intent = Intent(requireContext(), SearchBookActivity::class.java)
            startActivity(intent)
        }
        setupBackPressedCallback()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (listAdapter.isSelectionMode()) {
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
                            // 여기서 전처리.
                            val dummyList = addDummyItems(state.books)
                            val paddedList = updateShelfPosition(dummyList)
                            handleLibraryState(paddedList)
                            
                            // 선택된 bookId 처리
                            mainViewModel.selectedBookId?.let { bookId ->
                                val book = paddedList.find { it.itemId == bookId.value }
                                val position = paddedList.indexOf(book)
                                if (position != -1 && book != null) {
                                    val viewHolder = binding.rvList.findViewHolderForAdapterPosition(position)
                                    viewHolder?.itemView?.let { view ->
                                        handleItemClick(book, position, view.findViewById(R.id.cardView))
                                    }
//                                    mainViewModel.clearSelectedBookId()
                                }
                            }
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

    fun updateShelfPosition(originalList: List<BookUiModel>): List<BookUiModel> {
        return originalList.mapIndexed { index, book ->
            book.copy(shelfPosition = index % 3)
        }
    }

    fun addDummyItems(originalList: List<BookUiModel>): List<BookUiModel> {
        val remainder = originalList.size % 3
        return if (remainder == 0) {
            originalList
        } else {
            val paddingCount = 3 - remainder
            val padded = originalList.toMutableList()
            repeat(paddingCount) {
                padded.add(
                    BookUiModel(
                        itemId = -1 * (it + 1),
                    )
                )
            }
            padded
        }
    }

    private fun handleLibraryState(books: List<BookUiModel>) {
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
            rvList.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }
    }

    private fun showBookList(books: List<BookUiModel>) {
        binding.apply {
            rvList.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            listAdapter.submitList(books)
            initTransitionListener()
        }
    }

    private fun initTransitionListener() {
        binding.rvList.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.rvList.viewTreeObserver.removeOnPreDrawListener(this)
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

                val selectedViewHolder = binding.rvList.findViewHolderForAdapterPosition(currentPosition)
                selectedViewHolder?.let {
                    sharedElements[names[0]] = it.itemView.findViewById(R.id.cardView)
                }
            }
        })
    }

    private fun scrollToPosition(position: Int) {
        binding.rvList.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                binding.rvList.removeOnLayoutChangeListener(this)
                val layoutManager = binding.rvList.layoutManager
                val viewAtPosition = layoutManager?.findViewByPosition(position)

                if (viewAtPosition == null || layoutManager?.isViewPartiallyVisible(viewAtPosition, false, true) == true) {
                    binding.rvList.post {
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