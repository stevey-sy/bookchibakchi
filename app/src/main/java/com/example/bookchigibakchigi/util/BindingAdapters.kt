package com.example.bookchigibakchigi.util

import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.ui.bookdetail.adapter.BookViewPagerAdapter

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, imageUrl: String?) {
        Glide.with(view.context)
            .load(imageUrl)
            .apply(
                RequestOptions() // 로딩 중 표시할 기본 이미지
                .error(R.drawable.img_book_placeholder))            // 오류 시 표시할 이미지
            .into(view)
    }

//    @JvmStatic
//    @BindingAdapter("progressPercentage")
//    fun setProgressBarWidth(view: View, progress: Int) {
//        val parentWidth = (view.parent as ViewGroup).width // 부모 뷰의 전체 너비 가져오기
//        val newWidth = (parentWidth * progress) / 100      // 진행도에 따른 너비 계산
//        val layoutParams = view.layoutParams
//        layoutParams.width = newWidth
//        view.layoutParams = layoutParams
//    }

    @JvmStatic
    @BindingAdapter("submitBookList")
    fun bindBookList(viewPager: ViewPager2, bookList: List<BookEntity>?) {
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
    @BindingAdapter("progressPercentage")
    fun setProgressBarWidth(view: View, progress: Int) {
        view.post {
            // 부모 뷰의 전체 너비를 가져오기
            val parentWidth = (view.parent as ViewGroup).width
            val targetWidth = (parentWidth * progress) / 100 // 목표 너비 계산
            val currentWidth = view.layoutParams.width

            // ValueAnimator를 사용하여 애니메이션 구현
            val animator = ValueAnimator.ofInt(currentWidth, targetWidth)
            animator.duration = 300 // 애니메이션 지속 시간 (1초)
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                val layoutParams = view.layoutParams
                layoutParams.width = animatedValue
                view.layoutParams = layoutParams
            }
            animator.start()
        }
    }

    @JvmStatic
    @BindingAdapter("progressTranslation")
    fun setProgressTranslation(view: View, percentage: Int) {
        view.post {
            val parentView = view.parent as? ViewGroup
            val parentWidth = parentView?.width ?: 0 // 부모의 width 가져오기
            val viewWidth = view.width // 현재 뷰의 width

            // ✅ 100%일 때 부모 width를 초과하지 않도록 조정
            val baseTranslationX = (parentWidth * (percentage / 100f)).coerceAtMost(parentWidth - viewWidth.toFloat())

            // 목표 translationX 값 (자신의 width의 절반만큼 빼기)
            var targetTranslationX = baseTranslationX - (viewWidth / 2f)
            if(percentage == 100) {
                targetTranslationX = baseTranslationX - (viewWidth / 4f)
            }

            // 현재 translationX 값 가져오기
            val currentTranslationX = view.translationX

            // ValueAnimator로 애니메이션 실행
            val animator = ValueAnimator.ofFloat(currentTranslationX, targetTranslationX)
            animator.duration = 300 // 애니메이션 지속 시간 (300ms)
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                view.translationX = animatedValue // 애니메이션 값 적용
            }
            animator.start()
        }
    }


    @JvmStatic
    @BindingAdapter("bindBackgroundColor")
    fun bindBackgroundColor(view: View, color: LiveData<Int>?) {
        color?.observeForever { resolvedColor ->
            val context = view.context
            val newColor = ContextCompat.getColor(context, resolvedColor)

            // ✅ 현재 배경색 가져오기 (없으면 기본 투명색)
            val oldColor = (view.background as? ColorDrawable)?.color ?: ContextCompat.getColor(context, android.R.color.transparent)

            // ✅ ValueAnimator를 사용한 색상 변화 애니메이션 적용
            ValueAnimator.ofArgb(oldColor, newColor).apply {
                duration = 500 // 0.5초 동안 애니메이션 적용
                addUpdateListener { animator ->
                    view.setBackgroundColor(animator.animatedValue as Int)
                }
                start() // 애니메이션 시작
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bindTextColor")
    fun bindTextColor(view: TextView, color: LiveData<Int>?) {
        color?.observeForever { resolvedColor ->
            view.setTextColor(view.context.getColor(resolvedColor))
        }
    }

    @JvmStatic
    @BindingAdapter("bindSrc")
    fun bindSrc(view: ImageView, src: LiveData<Int>?) {
        src?.observeForever { resolvedSrc ->
            view.setImageResource(resolvedSrc)
        }
    }

    @JvmStatic
    @BindingAdapter("bindPauseButtonIcon")
    fun bindPauseButtonIcon(view: ImageView, icon: Int?) {
        icon?.let {
            view.setImageResource(it) // ✅ 아이콘 변경
        }
    }
}