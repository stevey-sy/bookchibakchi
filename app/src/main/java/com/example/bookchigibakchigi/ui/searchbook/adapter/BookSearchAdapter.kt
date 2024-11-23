package com.example.bookchigibakchigi.ui.searchbook.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.databinding.ItemBookSearchBinding
import com.example.bookchigibakchigi.network.model.BookItem

class BookSearchAdapter(
    private val onBookClick: (BookItem, View) -> Unit
) : ListAdapter<BookItem, BookSearchAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class BookViewHolder(private val binding: ItemBookSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookItem: BookItem) {
            binding.book = bookItem  // 바인딩 객체에 데이터 설정
            binding.executePendingBindings()

            // 클릭 이벤트 처리
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookClick(bookItem, binding.llBookImage) // 람다 호출
                }
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<BookItem>() {
        override fun areItemsTheSame(oldItem: BookItem, newItem: BookItem): Boolean {
            return oldItem.isbn == newItem.isbn
        }

        override fun areContentsTheSame(oldItem: BookItem, newItem: BookItem): Boolean {
            return oldItem == newItem
        }
    }
}