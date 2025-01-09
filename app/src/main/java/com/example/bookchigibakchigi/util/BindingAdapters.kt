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
    @BindingAdapter("progressPercentage")
    fun setProgressBarWidth(view: View, progress: Int) {
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
}