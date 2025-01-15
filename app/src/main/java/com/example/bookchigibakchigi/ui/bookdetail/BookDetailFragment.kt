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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewPager2의 PageChangeCallback 설정
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