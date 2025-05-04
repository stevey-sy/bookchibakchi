package com.example.bookchigibakchigi.ui.addmemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R

class CardBackgroundAdapter(
    private val items: List<Int>,
    private val itemWidth: Int,
    private val onItemSelected: (Int) -> Unit,
) : RecyclerView.Adapter<CardBackgroundAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.ivBackgroundItem)

        fun bind(imageRes: Int) {
            // 아이템 크기 동적 설정
            val layoutParams = itemView.layoutParams
            layoutParams.width = itemWidth // 동적 너비 설정
            layoutParams.height = itemWidth // 정사각형으로 설정
            itemView.layoutParams = layoutParams

            if (imageRes == R.drawable.img_dummy) {
                imageView.setImageResource(R.drawable.img_dummy)
            } else {
                Glide.with(imageView.context)
                    .load(imageRes)
                    .centerCrop()
                    .into(imageView)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_background, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}