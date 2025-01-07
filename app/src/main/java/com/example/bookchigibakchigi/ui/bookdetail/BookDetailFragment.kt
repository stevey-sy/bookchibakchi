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
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AppDatabase와 BookShelfRepository 생성
        val bookDao = AppDatabase.getDatabase(requireContext()).bookDao()
        val repository = BookShelfRepository(bookDao)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this, MyLibraryViewModelFactory(repository))
            .get(MyLibraryViewModel::class.java)

        adapter = BookViewPagerAdapter()
        binding.viewPager.adapter = adapter

        // Observe LiveData
        viewModel.bookShelfItems.observe(viewLifecycleOwner) { bookList ->
            adapter.setDataList(bookList)
        }

        // Observe 현재 선택된 책 데이터
        viewModel.currentBook.observe(viewLifecycleOwner) { currentBook ->
            binding.tvBookTitle.text = currentBook.title
            binding.tvAuthor.text = currentBook.author
            binding.tvPublisher.text = currentBook.publisher
        }

        // ViewPager2의 PageChangeCallback 설정
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updateCurrentBook(position) // 선택된 페이지 업데이트
            }
        })

        // 초기 진행률과 목표 진행률 설정
        val initialProgress = 0
        val targetProgress = 70 // 목표 진행률 (%)

        // 프로그레스바 배경의 전체 길이 가져오기
        binding.progressBarBackground.post {
            val totalWidth = binding.progressBarBackground.width

            // 애니메이션: 프로그레스바의 너비 늘리기
            val progressBarAnimation = ValueAnimator.ofInt(initialProgress, targetProgress)
            progressBarAnimation.duration = 1000 // 애니메이션 시간 (1초)
            progressBarAnimation.addUpdateListener { animator ->
                val progress = animator.animatedValue as Int
                val progressWidth = (totalWidth * progress) / 100

                // 프로그레스바 길이 업데이트
                val params = binding.progressBarForeground.layoutParams
                params.width = progressWidth
                binding.progressBarForeground.layoutParams = params

                // 진행 퍼센트 텍스트 위치와 내용 업데이트
                binding.tvProgressPercentage.translationX = progressWidth.toFloat()
                binding.tvProgressPercentage.text = "$progress%"
            }
            progressBarAnimation.start()
        }

    }
}