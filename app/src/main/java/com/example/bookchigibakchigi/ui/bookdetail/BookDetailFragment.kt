package com.example.bookchigibakchigi.ui.bookdetail

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentBookDetailBinding
import com.example.bookchigibakchigi.ui.MainActivityViewModel
import com.example.bookchigibakchigi.ui.bookdetail.adapter.BookViewPagerAdapter
import com.example.bookchigibakchigi.ui.record.RecordActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var adapter: BookViewPagerAdapter
    private var sharedView: View? = null
    private var currentPagePosition = 0

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

        // ViewModel 바인딩
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = BookViewPagerAdapter(
            transitionName,
            onItemClick = { bookEntity, position, sharedView ->
                // 아이템 클릭 이벤트 처리
                Log.d("TEST TEST","클릭된 아이템: ${bookEntity.itemId}, Position: $position")
            },
            onImageLoaded = {
                startPostponedEnterTransition()
            }
        )
        binding.viewPager.adapter = adapter

        // ViewModel 데이터 관찰 및 초기 화면 설정
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList) // Adapter에 데이터 설정

            viewModel.currentBook.value?.let { currentBook ->
                val targetPosition = bookList.indexOfFirst { it.itemId == currentBook.itemId }

                val totalItems = bookList.size // 전체 아이템 개수
                val hasEnoughItemsForPreview = targetPosition in 1 until totalItems - 1
                val isNearEnd = targetPosition >= totalItems - 3 // ✅ 마지막 3개 이내인지 확인

                // ✅ offscreenPageLimit을 동적으로 조정
                binding.viewPager.offscreenPageLimit = when {
                    isNearEnd -> 2
                    hasEnoughItemsForPreview -> 3
                    else -> 1
                }

                if (targetPosition != currentPagePosition) {
                    // ViewPager의 초기 Transition Name 설정
                    binding.viewPager.post {
                        if (targetPosition != currentPagePosition) {
                            binding.viewPager.setCurrentItem(targetPosition, false)
                        }
                        val currentPage = binding.viewPager.findViewWithTag<View>("page_$targetPosition")
                        currentPage?.findViewById<View>(R.id.ivBook)?.transitionName =
                            "sharedElement_${currentBook.itemId}"
                    }
                }
            }
        }

        binding.btnRecord.setOnClickListener {
            val selectedBook = viewModel.currentBook.value
            selectedBook?.let { book ->
                val intent = Intent(requireContext(), RecordActivity::class.java).apply {
                    putExtra("currentBook", book)
                }

                sharedView = binding.viewPager.findViewWithTag<View>("page_${binding.viewPager.currentItem}")?.findViewById(R.id.ivBook)
                sharedView!!.transitionName = "sharedView_${viewModel.currentBook.value?.itemId}"

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    sharedView!!,  // 시작점 (ViewPager의 ImageView)
                    sharedView!!.transitionName  // 동일한 transitionName 사용
                )
                startActivity(intent, options.toBundle())
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
            clipToPadding = false  // ✅ 양옆 페이지 보이게 설정
            clipChildren = false   // ✅ 양옆 페이지 보이게 설정
//            offscreenPageLimit = 3
//            (getChildAt(0) as RecyclerView).apply {
//                setItemViewCacheSize(Int.MAX_VALUE) // 모든 아이템을 미리 로드
//                recycledViewPool.clear() // 뷰 재사용 방지
//            }
            setPageTransformer(PreviewPageTransformer())
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentBook(position+1)
                sharedView = binding.viewPager.findViewWithTag<View>("page_$position")?.findViewById(R.id.ivBook)
                // 현재 ViewPager의 Transition Name과 position 저장
                currentPagePosition = position
                val currentItem = viewModel.currentBook.value
                val transitionName = "sharedView_${currentItem?.itemId}" // Transition Name 생성
                findNavController().previousBackStackEntry?.savedStateHandle?.set("current_transition_name", transitionName)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_position", position)
            }
        })
    }

    override fun onResume() {
        super.onResume()
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