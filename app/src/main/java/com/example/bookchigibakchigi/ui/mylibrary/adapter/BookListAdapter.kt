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
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.databinding.ItemBookShelfBinding
import com.example.bookchigibakchigi.util.VibrationUtil

class BookListAdapter(
    private val onItemClick: (BookUiModel, Int, View) -> Unit,
    private val onItemLongClick: (BookUiModel) -> Unit
) : ListAdapter<BookUiModel, BookListAdapter.BookListItemViewHolder>(BookDiffCallback()) {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<BookUiModel>()

    fun setSelectionMode(isSelectionMode: Boolean) {
        this.isSelectionMode = isSelectionMode
        if (!isSelectionMode) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun isSelectionMode(): Boolean = isSelectionMode

    fun getSelectedItems(): List<BookUiModel> = selectedItems.toList()

    fun toggleItemSelection(position: Int) {
        if (position < currentList.size) {
            val book = currentList[position]
            if (selectedItems.contains(book)) {
                selectedItems.remove(book)
            } else {
                selectedItems.add(book)
            }
            notifyItemChanged(position)
        }
    }

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
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bind(getItem(position), position)
        }
    }

    inner class BookListItemViewHolder(
        private val binding: ItemBookShelfBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(bookUiModel: BookUiModel, position: Int) {
            binding.apply {
                cardView.transitionName = "sharedView_${bookUiModel.itemId}"

                flDim.visibility = if (isSelectionMode && bookUiModel.itemId >= 0) View.VISIBLE else View.GONE
                checkBox.visibility = if (isSelectionMode && bookUiModel.itemId >= 0) View.VISIBLE else View.GONE
                checkBox.isChecked = selectedItems.contains(bookUiModel)
                cardView.visibility = View.GONE
                
                vBookShadow.visibility = View.INVISIBLE
                vBookShadowUp.visibility = View.INVISIBLE
                ivPlant.visibility = View.GONE
                ivClock.visibility = View.INVISIBLE

                if (bookUiModel.coverImageUrl.isNotEmpty()) {
                    Glide.with(root.context)
                        .load(bookUiModel.coverImageUrl)
                        .into(ivBook)
                    cardView.visibility = View.VISIBLE
                } else {
                    if (position == itemCount - 1) {
                        ivPlant.visibility = View.VISIBLE
                    }
                }

                val context = binding.root.context
                val drawableRes = when (bookUiModel.shelfPosition) {
                    0 -> R.drawable.shelf_left
                    1 -> R.drawable.shelf_center
                    2 -> R.drawable.shelf_right
                    else -> R.drawable.shelf_center
                }
                binding.vBottom.background = AppCompatResources.getDrawable(context, drawableRes)

                root.setOnClickListener {
                    if (isSelectionMode) {
                        toggleItemSelection(position)
                    } else {
                        if(bookUiModel.itemId < 0) return@setOnClickListener
                        onItemClick(bookUiModel, position, cardView)
                    }
                }

                root.setOnLongClickListener {
                    if (!isSelectionMode) {
                        VibrationUtil.vibrate(root.context)
                        toggleItemSelection(position)
                        onItemLongClick(bookUiModel)
                    }
                    true
                }

                checkBox.setOnClickListener {
                    if (isSelectionMode) {
                        toggleItemSelection(position)
                    }
                }
            }
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<BookUiModel>() {
        override fun areItemsTheSame(oldItem: BookUiModel, newItem: BookUiModel): Boolean {
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: BookUiModel, newItem: BookUiModel): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: BookUiModel, newItem: BookUiModel): Any? {
            return Any()
        }
    }
}
