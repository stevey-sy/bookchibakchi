package com.example.bookchigibakchigi.ui.searchbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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
        }
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

            // View 애니메이션
            if (position > lastAnimatedPosition) {
                binding.root.alpha = 0f
                binding.root.translationY = 30f
                binding.root.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()

                lastAnimatedPosition = adapterPosition
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
