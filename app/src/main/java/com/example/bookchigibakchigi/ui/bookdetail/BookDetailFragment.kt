package com.example.bookchigibakchigi.ui.bookdetail

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.databinding.FragmentBookDetailBinding
import com.example.bookchigibakchigi.repository.BookShelfRepository
import com.example.bookchigibakchigi.ui.bookdetail.adapter.BookViewPagerAdapter
import com.example.bookchigibakchigi.ui.mylibrary.MyLibraryViewModel
import com.example.bookchigibakchigi.ui.mylibrary.MyLibraryViewModelFactory

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyLibraryViewModel
    private lateinit var adapter: BookViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transition 설정
        val transition = TransitionInflater.from(context)
            .inflateTransition(R.transition.shared_element_transition)
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)

        // 전달된 itemId를 Bundle에서 가져오기
        val itemId = arguments?.getInt("itemId")
        val transitionName = arguments?.getString("transitionName") ?: ""

        // ViewModel 초기화
        val bookDao = AppDatabase.getDatabase(requireContext()).bookDao()
        val repository = BookShelfRepository(bookDao)
        viewModel = ViewModelProvider(this, MyLibraryViewModelFactory(repository))
            .get(MyLibraryViewModel::class.java)

        // ViewModel 바인딩
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = BookViewPagerAdapter(
            transitionName,
            onItemClick = { bookEntity, position, sharedView ->
                // 아이템 클릭 이벤트 처리
                println("클릭된 아이템: ${bookEntity.itemId}, Position: $position")
            },
            onImageLoaded = {
                startPostponedEnterTransition()
            }
        )
        binding.viewPager.adapter = adapter

        // ViewModel 데이터 관찰 및 초기 화면 설정
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList) // Adapter에 데이터 설정

            val initialPosition = bookList.indexOfFirst { it.itemId == itemId }
            if (initialPosition >= 0) {
                binding.viewPager.setCurrentItem(initialPosition, false)

                // ViewPager의 초기 Transition Name 설정
                binding.viewPager.post {
                    val currentPage =
                        binding.viewPager.findViewWithTag<View>("page_$initialPosition")
                    currentPage?.findViewById<View>(R.id.ivBook)?.transitionName =
                        "sharedElement_${itemId}"
                }
            }
        }

        prepareSharedElementTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        // 시스템 Back 버튼 동작 커스터마이징
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentPosition = binding.viewPager.currentItem
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", currentPosition)
            findNavController().popBackStack()
        }

        binding.viewPager.clipToPadding = false
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        binding.viewPager.setPadding(pageMarginPx, 0, pageMarginPx, 0)

        binding.viewPager.apply {
            offscreenPageLimit = 3 // 양 옆의 아이템을 미리 렌더링
            setPageTransformer(PreviewPageTransformer())
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updateCurrentBook(position) // 선택된 페이지 업데이트

                // 현재 ViewPager의 Transition Name과 position 저장
                val currentItem = viewModel.currentBook.value
                val transitionName = "sharedView_${currentItem?.itemId}" // Transition Name 생성
                findNavController().previousBackStackEntry?.savedStateHandle?.set("current_transition_name", transitionName)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", position)
            }
        })
    }

    fun refreshContent() {
        // 필요한 데이터를 다시 가져오거나, 화면을 다시 그립니다.
        viewModel.reloadBooks() // ViewModel에서 데이터 로드 메서드 호출
        adapter.notifyDataSetChanged() // ViewPager의 데이터 업데이트
    }

    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private fun prepareSharedElementTransition() {
        val transition =
            TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition

        setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // ViewPager에서 현재 선택된 페이지의 Transition Name 가져오기
                    val currentPosition = binding.viewPager.currentItem
                    val currentItem = viewModel.currentBook.value

                    if (currentItem == null) return

                    val transitionName = "sharedView_${currentItem.itemId}"

                    // ViewPager의 현재 페이지 View를 찾음
                    val currentPage = binding.viewPager.findViewWithTag<View>("page_$currentPosition")
                    val imageView = currentPage?.findViewById<View>(R.id.ivBook)

                    if (imageView != null) {
                        // names 리스트의 첫 번째 Transition Name에 매핑
                        sharedElements[names[0]] = imageView
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}