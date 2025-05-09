package com.example.bookchigibakchigi.ui.searchbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R

class ShimmerAdapter(private val itemCount: Int = 6) : RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    inner class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_search_shimmer, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // 바인딩할 데이터 없음 (로딩 상태용)
    }

    override fun getItemCount(): Int = itemCount
}
