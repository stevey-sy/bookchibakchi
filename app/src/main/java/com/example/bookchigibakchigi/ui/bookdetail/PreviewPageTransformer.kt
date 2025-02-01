package com.example.bookchigibakchigi.ui.bookdetail

import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchigibakchigi.R
import kotlin.math.abs

class PreviewPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageIdentifier = page.tag ?: "Page-${page.hashCode()}"
            Log.d("ViewPagerTransform", "Page: $pageIdentifier, Position: $position")

            // ✅ position이 0에 가깝다면 강제로 scale 1.0 유지
            val scaleFactor = if (abs(position) == 0.0f) {
                1.0f
            } else {
                MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
            }

            scaleX = scaleFactor
            scaleY = scaleFactor

            alpha = 1f // ✅ 투명도 유지
        }
    }

    companion object {
        private const val MIN_SCALE = 0.85f
    }
}