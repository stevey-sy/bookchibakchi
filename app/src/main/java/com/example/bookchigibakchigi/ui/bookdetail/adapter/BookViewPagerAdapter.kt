package com.example.bookchigibakchigi.ui.bookdetail.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ItemViewPagerBinding

class BookViewPagerAdapter : RecyclerView.Adapter<BookViewPagerAdapter.BookViewPagerViewHolder>(){

    private val dataList = mutableListOf<BookEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewPagerViewHolder {
        val binding = ItemViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewPagerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: BookViewPagerViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    fun setDataList(newList: List<BookEntity>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    class BookViewPagerViewHolder(private val binding: ItemViewPagerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookEntity: BookEntity) {
            if (bookEntity.coverImageUrl.isNotEmpty()) {
                binding.ivBook.visibility = View.VISIBLE
                Glide.with(binding.ivBook.context)
                    .load(bookEntity.coverImageUrl)
                    .into(binding.ivBook)
            }
        }

    }

}