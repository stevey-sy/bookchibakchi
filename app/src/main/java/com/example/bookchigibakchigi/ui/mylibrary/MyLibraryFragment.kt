package com.example.bookchigibakchigi.ui.mylibrary

import android.graphics.Rect
import android.os.Bundle
import android.transition.TransitionInflater
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
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding
import com.example.bookchigibakchigi.ui.main.MainViewModel
import com.example.bookchigibakchigi.ui.main.MainViewUiState
import com.example.bookchigibakchigi.ui.mylibrary.adapter.BookShelfAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null

    private val binding get() = _binding!!
    private lateinit var adapter: BookShelfAdapter

    private val mainViewModel: MainViewModel by activityViewModels()

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
    }

    private fun initBinding() {
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun initRecyclerView() {
        adapter = BookShelfAdapter { bookEntity, position, sharedView ->
            val bundle = Bundle().apply {
                putInt("itemId", bookEntity.itemId)
                putString("transitionName", "sharedView_${bookEntity.itemId}")
            }

            mainViewModel.setCurrentBook(bookEntity)

            val extras = FragmentNavigatorExtras(
                sharedView to "sharedView_${bookEntity.itemId}"
            )

            findNavController().navigate(R.id.action_myLibrary_to_bookDetail, bundle, null, extras)
        }
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
                        else -> {
                            // 다른 상태는 무시
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
            // 데이터 로드 완료 후 Transition 시작
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

//    private fun prepareTransitions() {
//        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.exit_transition)
//
//        setExitSharedElementCallback(object : SharedElementCallback() {
//            override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
//                Log.d("MyLibraryFragment", "onMapSharedElements called with names: $names")
//                val currentTransitionName = names.firstOrNull() ?: return
//                Log.d("MyLibraryFragment", "currentTransitionName: $currentTransitionName")
//                val selectedViewHolder = binding.rvShelf.findViewHolderForAdapterPosition(0)
//                if (selectedViewHolder != null) {
//                    sharedElements[currentTransitionName] = selectedViewHolder.itemView.findViewById(R.id.ivBook)
//                    Log.d("MyLibraryFragment", "Shared element mapped successfully")
//                } else {
//                    Log.d("MyLibraryFragment", "SelectedViewHolder is null")
//                }
//            }
//        })
//    }

    private fun prepareTransitions() {
        //exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.exit_transition)

        setExitSharedElementCallback(object : androidx.core.app.SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
                // 최신 Transition Name과 position 가져오기
                val currentTransitionName = findNavController()
                    .currentBackStackEntry?.savedStateHandle?.get<String>("current_transition_name")
                val currentPosition = findNavController()
                    .currentBackStackEntry?.savedStateHandle?.get<Int>("selected_position") ?: -1

                if (currentTransitionName.isNullOrEmpty() || currentPosition == -1) return

                // RecyclerView의 ViewHolder 찾기
                val selectedViewHolder = binding.rvShelf.findViewHolderForAdapterPosition(currentPosition)
                scrollToPosition(currentPosition)
                sharedElements[names[0]] =
                    selectedViewHolder!!.itemView.findViewById(R.id.ivBook)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}