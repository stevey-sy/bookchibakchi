package com.example.bookchigibakchigi.ui.mylibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
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
        holder.bind(getItem(position), position)
    }

    override fun onBindViewHolder(
        holder: BookListItemViewHolder,
        position: Int,
        payloads: MutableList<Any>) {
//        holder.bind(getItem(position), position)
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // ⚡ 위치 바뀐 아이템도 강제로 다시 바인딩
            holder.bind(getItem(position), position)
        }
    }

    inner class BookListItemViewHolder(
        private val binding: ItemBookShelfBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(bookEntity: BookEntity, position: Int) {
            binding.apply {
                cardView.transitionName = "sharedView_${bookEntity.itemId}"

                // 선택 모드 관련 UI 숨김
                flDim.visibility = View.GONE
                checkBox.visibility = View.GONE
                cardView.visibility = View.GONE
                
                // 책장 관련 UI 숨김
//                vBottom.visibility = View.GONE
                vBookShadow.visibility = View.INVISIBLE
                vBookShadowUp.visibility = View.INVISIBLE
                ivPlant.visibility = View.GONE
                ivClock.visibility = View.INVISIBLE

                if (bookEntity.coverImageUrl.isNotEmpty()) {
                    Glide.with(root.context)
                        .load(bookEntity.coverImageUrl)
                        .into(ivBook)
                    cardView.visibility = View.VISIBLE
                } else {
                    // 마지막 position 이면 ivPlant 보이기
                    if (position == itemCount - 1) {
                        ivPlant.visibility = View.VISIBLE
                    }
                }

                val context = binding.root.context
                val drawableRes = when (position % 3) {
                    0 -> R.drawable.shelf_left // 왼쪽 아이템의 Drawable
                    1 -> R.drawable.shelf_center // 가운데 아이템의 Drawable
                    2 -> R.drawable.shelf_right // 오른쪽 아이템의 Drawable
                    else -> R.drawable.shelf_center // 기본 Drawable (예외 처리용)
                }
                binding.vBottom.background = AppCompatResources.getDrawable(context, drawableRes)

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

        override fun getChangePayload(oldItem: BookEntity, newItem: BookEntity): Any? {
            // 데이터는 같아도 위치가 바뀌면 payload 반환
            return Any()
        }
    }
}
