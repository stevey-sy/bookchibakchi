package com.example.bookchigibakchigi.util

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
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
            // 부모 뷰 가져오기
            val parentView = view.parent as? ViewGroup
            val parentWidth = parentView?.width ?: 0 // 부모의 width 가져오기

            // 부모의 width를 기준으로 percentage에 따른 목표 translationX 계산
            val baseTranslationX = parentWidth * (percentage / 100f)

            // 목표 translationX 값 (자신의 width의 절반만큼 빼기)
            val targetTranslationX = baseTranslationX - (view.width / 2f)

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
}