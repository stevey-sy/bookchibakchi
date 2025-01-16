package com.example.bookchigibakchigi.ui.bookdetail

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import kotlin.math.abs

class PreviewPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageTranslationX = resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
            translationX = -position * pageTranslationX

            // 아이템 크기 축소 및 투명도 조정
            val scaleFactor = MIN_SCALE.coerceAtLeast(1 - abs(position))
            scaleY = scaleFactor
            scaleX = scaleFactor
            alpha = scaleFactor
        }
    }

    companion object {
        private const val MIN_SCALE = 0.8f
    }
}