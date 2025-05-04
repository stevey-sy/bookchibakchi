package com.example.bookchigibakchigi.ui.card

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class CustomSnapHelper(private val context: Context) : PagerSnapHelper() {
//    override fun createScroller(layoutManager: RecyclerView.LayoutManager?): RecyclerView.SmoothScroller? {
//        return if (layoutManager is LinearLayoutManager) {
//            object : LinearSmoothScroller(context) {
//                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
//                    return 100f / displayMetrics.densityDpi // ✅ 속도를 조절 (값을 늘리면 애니메이션이 더 강해짐)
//                }
//            }
//        } else {
//            null
//        }
//    }
}