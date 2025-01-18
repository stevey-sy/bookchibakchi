package com.example.bookchigibakchigi.ui.bookdetail

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)

        // 전달된 itemId를 Bundle에서 가져오기
        val itemId = arguments?.getInt("itemId")

        // 가져온 itemId 사용
        if (itemId != null) {
            println("전달받은 itemId: $itemId")
        } else {
            println("itemId가 전달되지 않았습니다.")
        }

        // ViewModel 초기화
        val bookDao = AppDatabase.getDatabase(requireContext()).bookDao()
        val repository = BookShelfRepository(bookDao)
        viewModel = ViewModelProvider(this, MyLibraryViewModelFactory(repository))
            .get(MyLibraryViewModel::class.java)

        // ViewModel 바인딩
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = BookViewPagerAdapter()
        binding.viewPager.adapter = adapter

        // ViewModel 데이터 관찰 및 초기 화면 설정
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList) // Adapter에 데이터 설정

            // 전달된 itemId로 position 찾기
            itemId?.let { id ->
                val initialPosition = bookList.indexOfFirst { it.itemId == id }
                if (initialPosition >= 0) {
                    // ViewPager의 현재 아이템을 설정
                    binding.viewPager.setCurrentItem(initialPosition, false)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewPager2 미리보기 설정
        binding.viewPager.clipToPadding = false
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        binding.viewPager.setPadding(pageMarginPx, 0, pageMarginPx, 0)

        binding.viewPager.apply {
            offscreenPageLimit = 2 // 양 옆의 아이템을 미리 렌더링
            setPageTransformer(PreviewPageTransformer())
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updateCurrentBook(position) // 선택된 페이지 업데이트
            }
        })
    }

    fun refreshContent() {
        // 필요한 데이터를 다시 가져오거나, 화면을 다시 그립니다.
        viewModel.reloadBooks() // ViewModel에서 데이터 로드 메서드 호출
        adapter.notifyDataSetChanged() // ViewPager의 데이터 업데이트
    }
}