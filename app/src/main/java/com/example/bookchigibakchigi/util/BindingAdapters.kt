package com.example.bookchigibakchigi.util

import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.ui.bookdetail.adapter.BookViewPagerAdapter
import com.example.bookchigibakchigi.ui.main.MainViewUiState
import com.example.bookchigibakchigi.ui.record.RecordUiState
import kotlinx.coroutines.flow.StateFlow

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, imageUrl: String?) {
        Glide.with(view.context)
            .load(imageUrl)
//            .apply(
//                RequestOptions() // 로딩 중 표시할 기본 이미지
//                .error(R.drawable.img_book_placeholder))            // 오류 시 표시할 이미지
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("submitBookList")
    fun bindBookList(viewPager: ViewPager2, bookList: List<BookUiModel>?) {
        if (bookList == null) return
        val adapter = viewPager.adapter
        if (adapter is BookViewPagerAdapter) {
            adapter.setDataList(bookList)
        }
    }

    @JvmStatic
    @BindingAdapter("isVisible")
    fun setVisibility(view: View, items: List<*>?) {
        view.visibility = if (items.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("isProgressBarVisible")
    fun setProgressBarVisibleVisibility(view: View, items: List<*>?) {
        view.visibility = if (items.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("bindPauseButtonIcon")
    fun bindPauseButtonIcon(view: ImageView, icon: Int?) {
        icon?.let {
            view.setImageResource(it) // ✅ 아이콘 변경
        }
    }

    // BookDetailFragment.kt
    @JvmStatic
    @BindingAdapter("bookTitle")
    fun TextView.setBookTitle(uiState: MainViewUiState?) {
        text = when (uiState) {
            is MainViewUiState.BookDetail -> {
                uiState.currentBook.title
            }

            else -> {
                ""
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bookAuthor")
    fun TextView.setBookAuthor(uiState: MainViewUiState?) {
        text = when (uiState) {
            is MainViewUiState.BookDetail -> {
                uiState.currentBook.author
            }

            else -> {
                ""
            }
        }
    }

    @JvmStatic
    @BindingAdapter("percentage")
    fun TextView.setPercentage(uiState: MainViewUiState?) {
        text = when (uiState) {
            is MainViewUiState.BookDetail -> {
                uiState.currentBook.getPercentageStr()
            }
            else -> {
                ""
            }
        }
    }

    @JvmStatic
    @BindingAdapter("page")
    fun TextView.setPageProgress(uiState: MainViewUiState?) {
        text = when (uiState) {
            is MainViewUiState.BookDetail -> {
                uiState.currentBook.progressText
            }
            else -> {
                ""
            }
        }
    }

    @JvmStatic
    @BindingAdapter("comment")
    fun TextView.setComment(uiState: MainViewUiState?) {
        text = when (uiState) {
            is MainViewUiState.BookDetail -> {
//                uiState.photoCards.size.toString()
//                if(uiState.photoCards.isEmpty()) {
//                    "기록을 남겨보세요."
//                } else {
//                    "${uiState.photoCards.size} Comments"
//                }
                "${uiState.photoCards.size} Comments"
            }
            else -> {
                ""
            }
        }
    }

    @JvmStatic
    @BindingAdapter("progressPercentage")
    fun View.setProgressBarWidth(uiState: MainViewUiState?) {
        var percentage = 0
        when (uiState) {
            is MainViewUiState.BookDetail -> {
                percentage = uiState.currentBook.progressPercentage
                post {
                    // 부모 뷰의 전체 너비를 가져오기
                    val parentWidth = (parent as ViewGroup).width
                    val targetWidth = (parentWidth * percentage) / 100 // 목표 너비 계산
                    val currentWidth = layoutParams.width

                    // ValueAnimator를 사용하여 애니메이션 구현
                    val animator = ValueAnimator.ofInt(currentWidth, targetWidth)
                    animator.duration = 800 // 애니메이션 지속 시간 (1초)
                    animator.addUpdateListener { animation ->
                        val animatedValue = animation.animatedValue as Int
                        val newLayoutParams = layoutParams
                        newLayoutParams.width = animatedValue
                        layoutParams = newLayoutParams
                    }
                    animator.start()
                }
            }
            else -> {}
        }
    }

    @JvmStatic
    @BindingAdapter("progressTranslation")
    fun View.setProgressTranslation(uiState: MainViewUiState?) {
        var percentage = 0
        when (uiState) {
            is MainViewUiState.BookDetail -> {
                percentage = uiState.currentBook.progressPercentage
                post {
                    val parentView = parent as? ViewGroup
                    val parentWidth = parentView?.width ?: 0  // 부모 width 가져오기
                    var viewWidth = width // 현재 뷰의 width

                    // ⚠️ viewWidth가 0이면, 뷰 측정이 끝나지 않았을 가능성이 있음 → postDelayed 사용
                    if (viewWidth == 0) {
                        postDelayed({ setProgressTranslation(uiState) }, 50)
                        return@post
                    }

                    // ✅ 부모 width에 대한 비율로 translationX 계산
                    val baseTranslationX = (parentWidth * (percentage / 100f)).coerceAtMost(parentWidth.toFloat())

                    // ✅ 텍스트뷰의 width를 고려하여 중앙 정렬 (뷰 width의 절반만큼 빼기)
                    val targetTranslationX = (baseTranslationX - viewWidth / 2)

                    // 현재 translationX 값 가져오기
                    val currentTranslationX = translationX

                    // ValueAnimator로 애니메이션 실행
                    val animator = ValueAnimator.ofFloat(currentTranslationX, targetTranslationX)
                    animator.duration = 800 // 애니메이션 지속 시간 (300ms)
                    animator.addUpdateListener { animation ->
                        val animatedValue = animation.animatedValue as Float
                        translationX = animatedValue // 애니메이션 값 적용
                    }
                    animator.start()
                }
            }
            else -> {}
        }
    }

    @JvmStatic
    @BindingAdapter("bindBackgroundColor")
    fun View.bindBackgroundColor(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val context = context
                val newColor = when (flow.value) {
                    is RecordUiState.BeforeReading -> ContextCompat.getColor(context, R.color.white)
                    is RecordUiState.Reading -> ContextCompat.getColor(context, R.color.black)
                    is RecordUiState.Paused -> ContextCompat.getColor(context, R.color.white)
                    is RecordUiState.Completed -> ContextCompat.getColor(context, R.color.white)
                }

                // ✅ 현재 배경색 가져오기 (없으면 기본 투명색)
                val oldColor = (background as? ColorDrawable)?.color ?: ContextCompat.getColor(context, android.R.color.transparent)

                // ✅ ValueAnimator를 사용한 색상 변화 애니메이션 적용
                ValueAnimator.ofArgb(oldColor, newColor).apply {
                    duration = 500 // 0.5초 동안 애니메이션 적용
                    addUpdateListener { animator ->
                        setBackgroundColor(animator.animatedValue as Int)
                    }
                    start() // 애니메이션 시작
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindPlayBtn")
    fun ImageView.bindPlayBtn(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val newSrc = when (flow.value) {
                    is RecordUiState.BeforeReading -> R.drawable.ic_play_button
                    is RecordUiState.Reading -> R.drawable.ic_pause_white
                    is RecordUiState.Paused -> R.drawable.ic_play_button
                    is RecordUiState.Completed -> R.drawable.ic_play_button
                }
                setImageResource(newSrc)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindCloseBtn")
    fun ImageView.bindCloseBtn(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val newSrc = when (flow.value) {
                    is RecordUiState.BeforeReading -> R.drawable.ic_close_black
                    is RecordUiState.Reading -> R.drawable.ic_close_white
                    is RecordUiState.Paused -> R.drawable.ic_close_black
                    is RecordUiState.Completed -> R.drawable.ic_close_black
                }
                setImageResource(newSrc)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindTextColor")
    fun TextView.bindTextColor(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val newColor = when (flow.value) {
                    is RecordUiState.BeforeReading -> ContextCompat.getColor(context, R.color.black)
                    is RecordUiState.Reading -> ContextCompat.getColor(context, R.color.white)
                    is RecordUiState.Paused -> ContextCompat.getColor(context, R.color.black)
                    is RecordUiState.Completed -> ContextCompat.getColor(context, R.color.black)
                }
                setTextColor(newColor)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindReadingStatusText")
    fun TextView.bindReadingStatusText(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val text = when (flow.value) {
                    is RecordUiState.BeforeReading -> "독서를 시작해보세요"
                    is RecordUiState.Reading -> "읽고 있는 중..."
                    is RecordUiState.Paused -> "일시정지됨"
                    is RecordUiState.Completed -> "독서 완료!"
                }
                setText(text)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindCompleteBtnVisibility")
    fun View.bindCompleteBtnVisibility(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val visibility = when (flow.value) {
                    is RecordUiState.BeforeReading -> View.VISIBLE
                    is RecordUiState.Reading -> View.VISIBLE
                    is RecordUiState.Paused -> View.VISIBLE
                    is RecordUiState.Completed -> View.GONE
                }
                setVisibility(visibility)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindCongratsVisibility")
    fun View.bindCongratsVisibility(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            post {
                val visibility = when (flow.value) {
                    is RecordUiState.BeforeReading -> View.GONE
                    is RecordUiState.Reading -> View.GONE
                    is RecordUiState.Paused -> View.GONE
                    is RecordUiState.Completed -> View.VISIBLE
                }
                setVisibility(visibility)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindOutBtnVisibility")
    fun View.bindOutBtnVisibility(uiState: StateFlow<RecordUiState>?) {
        uiState?.let { flow ->
            android.util.Log.d("BindingAdapter", "bindOutBtnVisibility called with state: ${flow.value}")
            postDelayed({
                val visibility = when (flow.value) {
                    is RecordUiState.BeforeReading -> View.GONE
                    is RecordUiState.Reading -> View.GONE
                    is RecordUiState.Paused -> View.GONE
                    is RecordUiState.Completed -> View.VISIBLE
                }
                android.util.Log.d("BindingAdapter", "Setting visibility to: $visibility")
                setVisibility(visibility)
            }, 100)
        }
    }
}