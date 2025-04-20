package com.example.bookchigibakchigi.ui.mylibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ItemBookShelfBinding

class BookListAdapter(
    private val onItemClick: (BookEntity, View) -> Unit,
    private val onItemLongClick: (BookEntity) -> Unit
) : ListAdapter<BookEntity, BookListAdapter.BookListItemViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListItemViewHolder {
        val binding = ItemBookShelfBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookListItemViewHolder(
        private val binding: ItemBookShelfBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(bookEntity: BookEntity) {
            binding.apply {
                cardView.transitionName = "sharedView_${bookEntity.itemId}"
                
                if (bookEntity.coverImageUrl.isNotEmpty()) {
                    Glide.with(root.context)
                        .load(bookEntity.coverImageUrl)
                        .into(ivBook)
                }

                // 선택 모드 관련 UI 숨김
                flDim.visibility = View.GONE
                checkBox.visibility = View.GONE
                
                // 책장 관련 UI 숨김
                vBottom.visibility = View.GONE
                vBookShadow.visibility = View.INVISIBLE
                vBookShadowUp.visibility = View.INVISIBLE
                ivPlant.visibility = View.INVISIBLE
                ivClock.visibility = View.INVISIBLE

                root.setOnClickListener {
                    onItemClick(bookEntity, cardView)
                }

                root.setOnLongClickListener {
                    onItemLongClick(bookEntity)
                    true
                }
            }
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<BookEntity>() {
        override fun areItemsTheSame(oldItem: BookEntity, newItem: BookEntity): Boolean {
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: BookEntity, newItem: BookEntity): Boolean {
            return oldItem == newItem
        }
    }
}
