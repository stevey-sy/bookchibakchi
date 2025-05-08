package com.example.bookchigibakchigi.ui.searchbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ItemBookSearchBinding
import com.example.bookchigibakchigi.model.SearchBookUiModel
import com.example.bookchigibakchigi.ui.searchbook.adapter.BookPagingAdapter.BookViewHolder

class BookPagingAdapter(
    private val onBookClick: (SearchBookUiModel, View) -> Unit
) : PagingDataAdapter<SearchBookUiModel, BookViewHolder>(DIFF_CALLBACK) {

    private var lastAnimatedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBookSearchBinding.inflate(inflater, parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
            // 애니메이션 적용
            if (position > lastAnimatedPosition) {
                val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation_slide_right)
                holder.itemView.startAnimation(animation)
                lastAnimatedPosition = position
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: BookViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    suspend fun submitDataWithAnimation(pagingData: PagingData<SearchBookUiModel>) {
        lastAnimatedPosition = -1  // 새로운 데이터가 제출될 때 lastAnimatedPosition 초기화
        submitData(pagingData)
    }

    inner class BookViewHolder(private val binding: ItemBookSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookItem: SearchBookUiModel) {
            binding.book = bookItem  // 바인딩 객체에 데이터 설정
            binding.executePendingBindings()
            val position = bindingAdapterPosition
            // 클릭 이벤트 처리
            binding.root.setOnClickListener {
                val position = position
                if (position != RecyclerView.NO_POSITION) {
                    bookItem?.let {
                        onBookClick(it, binding.llBookImage) // ✅ null 아닌 경우에만 실행
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchBookUiModel>() {
            override fun areItemsTheSame(oldItem: SearchBookUiModel, newItem: SearchBookUiModel): Boolean {
                return oldItem.isbn == newItem.isbn
            }

            override fun areContentsTheSame(oldItem: SearchBookUiModel, newItem: SearchBookUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
