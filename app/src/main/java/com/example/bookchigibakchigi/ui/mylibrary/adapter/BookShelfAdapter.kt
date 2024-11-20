package com.example.bookchigibakchigi.ui.mylibrary.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.BookShelfItem
import com.example.bookchigibakchigi.databinding.ItemBookShelfBinding

class BookShelfAdapter : RecyclerView.Adapter<BookShelfAdapter.BookShelfItemViewHolder>() {

    private val dataList = mutableListOf<BookShelfItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookShelfItemViewHolder {
        val binding = ItemBookShelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookShelfItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: BookShelfItemViewHolder, position: Int) {
        holder.bind(dataList[position], position)
    }

    fun setDataList(newList: List<BookShelfItem>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    class BookShelfItemViewHolder(private val binding: ItemBookShelfBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookShelfItem: BookShelfItem, position: Int) {
            // 타입에 따른 뷰 가시성 처리
            binding.rlPlus.visibility = if (bookShelfItem.type == "0") View.VISIBLE else View.GONE
            // 열 위치에 따라 배경 Drawable 설정
            val context = binding.root.context
            val drawableRes = when (position % 3) {
                0 -> R.drawable.shelf_left // 왼쪽 아이템의 Drawable
                1 -> R.drawable.shelf_center // 가운데 아이템의 Drawable
                2 -> R.drawable.shelf_right // 오른쪽 아이템의 Drawable
                else -> R.drawable.shelf_center // 기본 Drawable (예외 처리용)
            }

            // 배경 Drawable 적용
            binding.vBottom.background = AppCompatResources.getDrawable(context, drawableRes)
            if(bookShelfItem.type == "0") return

            binding.ivBook.visibility = if (bookShelfItem.type == "0") View.GONE else View.VISIBLE
            if (bookShelfItem.imgUrl.isNotEmpty()) {
                binding.ivBook.visibility = View.VISIBLE
                Glide.with(binding.ivBook.context)
                    .load(bookShelfItem.imgUrl)
                    .into(binding.ivBook)
            }

        }
    }

}