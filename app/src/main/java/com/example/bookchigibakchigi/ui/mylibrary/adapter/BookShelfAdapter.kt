package com.example.bookchigibakchigi.ui.mylibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        holder.bind(dataList[position])
    }

    fun setDataList(newList: List<BookShelfItem>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    class BookShelfItemViewHolder(private val binding: ItemBookShelfBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookShelfItem: BookShelfItem) {
            binding.rlPlus.visibility = if (bookShelfItem.type == "0") View.VISIBLE else View.GONE
            if(bookShelfItem.type == "0") return

            binding.ivBook.visibility = if (bookShelfItem.type == "0") View.GONE else View.VISIBLE
            if (bookShelfItem.imgUrl.isNotEmpty()) {
                binding.ivBook.visibility = View.VISIBLE
                Glide.with(binding.ivBook.context)
                    .load(bookShelfItem.imgUrl) // URL을 여기 사용
//                .placeholder(R.drawable.placeholder_image) // 로딩 중 보여줄 이미지 (선택사항)
//                .error(R.drawable.error_image) // 에러 시 보여줄 이미지 (선택사항)
                    .into(binding.ivBook)
            }
        }
    }

}