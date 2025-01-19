package com.example.bookchigibakchigi.ui.mylibrary

import android.app.SharedElementCallback
import android.graphics.Rect
import android.os.Bundle
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.databinding.FragmentMyLibraryBinding
import com.example.bookchigibakchigi.repository.BookShelfRepository
import com.example.bookchigibakchigi.ui.MainActivity
import com.example.bookchigibakchigi.ui.mylibrary.adapter.BookShelfAdapter


class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null

    private val binding get() = _binding!!
    private lateinit var viewModel: MyLibraryViewModel
    private lateinit var adapter: BookShelfAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLibraryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        prepareTransitions();
        postponeEnterTransition();

        return root
    }

    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.exit_transition)

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
//                binding.rvShelf.post {
//                    val updatedViewHolder = binding.rvShelf.findViewHolderForAdapterPosition(currentPosition)
//                    if (updatedViewHolder?.itemView != null) {
//                        sharedElements[names[0]] =
//                            updatedViewHolder.itemView.findViewById(R.id.ivBook)
//                    }
//                }
                sharedElements[names[0]] =
                    selectedViewHolder!!.itemView.findViewById(R.id.ivBook)
            }
        })
    }
    override fun onResume() {
        super.onResume()
        viewModel.reloadBooks() // 데이터 갱신
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // AppDatabase와 BookShelfRepository 생성
        val bookDao = AppDatabase.getDatabase(requireContext()).bookDao()
        val repository = BookShelfRepository(bookDao)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this, MyLibraryViewModelFactory(repository))
            .get(MyLibraryViewModel::class.java)

        findNavController().currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Int>("selected_position").observe(viewLifecycleOwner) { position ->
                if (position != null) {
                    viewModel.updateCurrentBook(position) // ViewModel 업데이트
                    scrollToPosition(position) // RecyclerView 스크롤
                }
            }

            savedStateHandle.getLiveData<String>("current_transition_name").observe(viewLifecycleOwner) { transitionName ->
                // Transition Name 업데이트 로직이 필요하면 추가
            }
        }


        // RecyclerView 설정
        adapter = BookShelfAdapter { bookEntity, position, sharedView ->
            viewModel.updateCurrentBook(position)
            val bundle = Bundle().apply {
                putInt("itemId", bookEntity.itemId)
                putString("transitionName", "sharedView_${bookEntity.itemId}")
            }

            val extras = FragmentNavigatorExtras(
                sharedView to "sharedView_${bookEntity.itemId}" // transitionName과 일치해야 함
            )

            findNavController().navigate(R.id.action_myLibrary_to_bookDetail, bundle, null, extras)
        }
        binding.rvShelf.layoutManager = GridLayoutManager(context, 3)
        binding.rvShelf.adapter = adapter

        binding.rvShelf.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, 0, 0) // 모든 간격을 0으로 설정
            }
        })

        // Observe LiveData
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList)
            // 데이터 로드 완료 후 Transition 시작
            binding.rvShelf.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        binding.rvShelf.viewTreeObserver.removeOnPreDrawListener(this)
                        startPostponedEnterTransition() // Transition 시작
                        return true
                    }
                }
            )
        }
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