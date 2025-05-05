package com.example.bookchigibakchigi.ui.addmemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.FragmentAddMemoBackgroundBinding
import com.example.bookchigibakchigi.databinding.FragmentAddMemoPageBinding
import com.example.bookchigibakchigi.databinding.FragmentAddMemoQuoteBinding
import com.example.bookchigibakchigi.databinding.FragmentAddMemoTagBinding
import com.example.bookchigibakchigi.ui.card.CardActivity
import com.example.bookchigibakchigi.util.VibrationUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddMemoAdapter(
    private val onBackgroundChanged: (Int) -> Unit,
    private val onPageChanged: (String) -> Unit,
    private val onContentChanged: (String) -> Unit,
    private val onTagAdded: (String) -> Unit,
    private val backgroundImages: List<Int>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val BACKGROUND = 0
        const val PAGE_QUOTE = 1
        const val PAGE_PAGE = 2
        const val PAGE_TAG = 3
    }

    private var pageDebounceJob: Job? = null
    private var contentDebounceJob: Job? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BACKGROUND -> {
                val binding = FragmentAddMemoBackgroundBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                BackgroundViewHolder(binding)
            }
            PAGE_PAGE -> {
                val binding = FragmentAddMemoPageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PageViewHolder(binding)
            }
            PAGE_QUOTE -> {
                val binding = FragmentAddMemoQuoteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                QuoteViewHolder(binding)
            }
            PAGE_TAG -> {
                val binding = FragmentAddMemoTagBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TagViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BackgroundViewHolder -> holder.bind(onBackgroundChanged)
            is PageViewHolder -> holder.bind(onPageChanged)
            is QuoteViewHolder -> holder.bind(onContentChanged)
            is TagViewHolder -> holder.bind(onTagAdded)
        }
    }

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> BACKGROUND
            1 -> PAGE_QUOTE
            2 -> PAGE_PAGE
            3 -> PAGE_TAG
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    inner class BackgroundViewHolder(private val binding: FragmentAddMemoBackgroundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onBackgroundChanged: (Int) -> Unit) {
            // 화면 너비 계산
            val displayMetrics = binding.root.context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val padding = binding.root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp) * 2 // 좌우 패딩
            val itemSpacing = binding.root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp) * 4 // 아이템 간 간격 (5개 아이템이면 4개의 간격)
            
            // 5개의 아이템이 보이도록 itemWidth 계산
            val itemWidth = (screenWidth - padding - itemSpacing) / 5
            
            // CardBackgroundAdapter 초기화
            val backgroundAdapter = CardBackgroundAdapter(
                items = backgroundImages,
                itemWidth = itemWidth,
                onItemSelected = { position ->
                    onBackgroundChanged(position)
                }
            )
            
            binding.rvBackground.apply {
                adapter = backgroundAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            // PagerSnapHelper 적용
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.rvBackground)

            // llAim의 렌더링 완료 후 RecyclerView 초기 위치 설정
            val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // llAim의 크기와 위치가 설정된 후 실행
                    binding.llAim.viewTreeObserver.removeOnGlobalLayoutListener(this) // 여기서 'this'는 리스너 객체를 참조

                    // llAim의 시작점
                    val llAimStart = binding.llAim.left
                    Log.d("DEBUG", "llAimStart: $llAimStart") // llAim의 시작점 로그 출력

                    // RecyclerView 세 번째 아이템의 시작점
                    val layoutManager = binding.rvBackground.layoutManager as LinearLayoutManager
                    val snapView = layoutManager.findViewByPosition(2) // 세 번째 아이템 뷰

                    // SnapView(세 번째 아이템)가 존재할 경우 위치 조정
                    snapView?.let {
                        val rvItemStart = it.left
                        Log.d("DEBUG", "rvBackground third item start: $rvItemStart") // 세 번째 아이템의 시작점 로그 출력

                        // 오프셋 계산: llAimStart와 rvItemStart의 차이만큼 조정
                        val offset = llAimStart - rvItemStart
                        Log.d("DEBUG", "Offset to adjust: $offset") // 오프셋 로그 출력

                        // 스크롤 이동: 세 번째 아이템을 llAim에 정확히 맞춤
                        layoutManager.scrollToPositionWithOffset(0, offset)

                        binding.llAim.bringToFront()

                        // SnapHelper로 중앙 위치 강제 스냅
                        snapHelper.findSnapView(binding.rvBackground.layoutManager)?.let { snapView ->
                            val position = layoutManager.getPosition(snapView)
//                            if (position != lastSelectedPosition) {
//                                lastSelectedPosition = position
//                                Glide.with(this@CardActivity).load(paddedImages[position]).into(binding.ivBackground)
//                            }
                        }
                    }
                }
            }

            // ViewTreeObserver에 리스너 추가
            binding.llAim.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

            // 스크롤이 멈추면 선택된 아이템 감지 + 진동 효과 추가
            binding.rvBackground.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val snapView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(snapView)
                        onBackgroundChanged(position)
//                        if (position != lastSelectedPosition) { // 새로운 아이템이 선택되었을 때만 실행
//                            lastSelectedPosition = position
//                            VibrationUtil.vibrate(this@CardActivity, 100) // ✅ 진동 효과 실행
//
//                            // 선택된 이미지 변경
//                            Glide.with(this@CardActivity)
//                                .load(paddedImages[position])
//                                .into(binding.ivBackground)
//
//                            binding.etBookTitle.bringToFront();
//                        }
                    }
                }
            })
        }
    }

    inner class PageViewHolder(private val binding: FragmentAddMemoPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onPageChanged: (String) -> Unit) {
            binding.etPage.doAfterTextChanged { text ->
                pageDebounceJob?.cancel()
                pageDebounceJob = kotlinx.coroutines.MainScope().launch {
                    delay(300)
                    onPageChanged(text.toString())
                }
            }
        }
    }

    inner class QuoteViewHolder(private val binding: FragmentAddMemoQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onContentChanged: (String) -> Unit) {
            binding.etContent.doAfterTextChanged { text ->
                contentDebounceJob?.cancel()
                contentDebounceJob = kotlinx.coroutines.MainScope().launch {
                    delay(300)
                    onContentChanged(text.toString())
                }
            }
        }
    }

    inner class TagViewHolder(private val binding: FragmentAddMemoTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onTagAdded: (String) -> Unit) {
            binding.etTag.doAfterTextChanged { text ->
                // 태그 입력 관련 처리
            }

            binding.btnAddTag.setOnClickListener {
                val tagName = binding.etTag.text.toString().trim()
                if (tagName.isNotEmpty()) {
                    onTagAdded(tagName)
                    binding.etTag.text.clear()
                }
            }
        }
    }
} 