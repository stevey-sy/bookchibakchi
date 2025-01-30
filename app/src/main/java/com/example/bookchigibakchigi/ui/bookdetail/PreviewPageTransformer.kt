package com.example.bookchigibakchigi.ui.bookdetail

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import kotlin.math.abs

class PreviewPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageTranslationX = resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
            translationX = position * -pageTranslationX

            // ✅ 현재 선택된 페이지는 100% 크기 유지
            val scaleFactor = if (position == 0f) {
                1.0f
            } else {
                MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
            }

            scaleX = scaleFactor
            scaleY = scaleFactor

            // ✅ 투명도 조정: 현재 페이지는 1.0, 양 옆 페이지는 점점 투명
            alpha = 0.5f + ((1 - abs(position)) * 0.5f)
        }
    }

    companion object {
        private const val MIN_SCALE = 0.85f // ✅ 너무 작아지지 않도록 조정
    }
}