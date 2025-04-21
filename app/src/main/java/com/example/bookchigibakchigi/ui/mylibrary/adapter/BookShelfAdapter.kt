package com.example.bookchigibakchigi.ui.mylibrary.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.databinding.ItemBookShelfBinding
import com.example.bookchigibakchigi.util.VibrationUtil

class BookShelfAdapter(
    private val onItemClick: (BookUiModel, Int, View) -> Unit,
    private val onItemLongClick: (BookUiModel) -> Unit
) : RecyclerView.Adapter<BookShelfAdapter.BookShelfItemViewHolder>() {

    private val dataList = mutableListOf<BookUiModel>()
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
        if (position < dataList.size) {
            val book = dataList[position]
            if (selectedItems.contains(book)) {
                selectedItems.remove(book)
            } else {
                selectedItems.add(book)
            }
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookShelfItemViewHolder {
        val binding = ItemBookShelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookShelfItemViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        val rowCount = Math.ceil(dataList.size / 3.0).toInt()
        return maxOf(9, rowCount * 3)
    }

    override fun onBindViewHolder(holder: BookShelfItemViewHolder, position: Int) {
        if(dataList.isEmpty()) return
        if (position < dataList.size) {
            holder.bind(dataList[position], position, onItemClick, onItemLongClick)
        } else {
            holder.bindEmpty(position, itemCount)
        }
    }

    fun setDataList(newList: List<BookUiModel>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    class BookShelfItemViewHolder(
        private val binding: ItemBookShelfBinding,
        private val adapter: BookShelfAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookUiModel: BookUiModel, position: Int, onItemClick: (BookUiModel, Int, View) -> Unit, onItemLongClick: (BookUiModel) -> Unit) {
            binding.cardView.transitionName = "sharedView_${bookUiModel.itemId}"
            Log.d("transitionName test", binding.cardView.transitionName)

            binding.root.setOnClickListener {
                if (adapter.isSelectionMode()) {
                    adapter.toggleItemSelection(position)
                } else {
                    onItemClick(bookUiModel, position, binding.cardView)
                }
            }

            binding.checkBox.setOnClickListener {
                if (adapter.isSelectionMode()) {
                    adapter.toggleItemSelection(position)
                }
            }

            binding.root.setOnLongClickListener {
                if (!adapter.isSelectionMode()) {
                    VibrationUtil.vibrate(binding.root.context)
                    adapter.toggleItemSelection(position)
                    onItemLongClick(bookUiModel)
                }
                true
            }

            binding.flDim.visibility = if (adapter.isSelectionMode()) View.VISIBLE else View.GONE
            binding.checkBox.visibility = if (adapter.isSelectionMode()) View.VISIBLE else View.GONE
            binding.checkBox.isChecked = adapter.getSelectedItems().contains(bookUiModel)

            val context = binding.root.context
            val drawableRes = when (position % 3) {
                0 -> R.drawable.shelf_left
                1 -> R.drawable.shelf_center
                2 -> R.drawable.shelf_right
                else -> R.drawable.shelf_center
            }

            binding.vBottom.background = AppCompatResources.getDrawable(context, drawableRes)
            binding.cardView.visibility = View.VISIBLE
            binding.ivBook.visibility = View.VISIBLE
            binding.vBookShadow.visibility = View.INVISIBLE
            binding.vBookShadowUp.visibility = View.INVISIBLE
            binding.ivPlant.visibility = View.INVISIBLE
            binding.ivClock.visibility = View.INVISIBLE
            binding.ivClock.visibility = View.INVISIBLE

            if (bookUiModel.coverImageUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(bookUiModel.coverImageUrl)
                    .into(binding.ivBook)
            }
        }

        fun bindEmpty(position: Int, itemCount: Int) {
            binding.cardView.transitionName = "sharedView_"
            binding.flDim.visibility = if (adapter.isSelectionMode()) View.VISIBLE else View.GONE
            binding.checkBox.visibility = if (adapter.isSelectionMode()) View.VISIBLE else View.GONE

            binding.ivBook.visibility = View.INVISIBLE
            binding.cardView.visibility = View.INVISIBLE
            binding.vBookShadow.visibility = View.INVISIBLE
            binding.vBookShadowUp.visibility = View.INVISIBLE
            binding.ivPlant.visibility = if (position == itemCount - 1) View.VISIBLE else View.INVISIBLE

            val context = binding.root.context
            val drawableRes = when (position % 3) {
                0 -> R.drawable.shelf_left
                1 -> R.drawable.shelf_center
                2 -> R.drawable.shelf_right
                else -> R.drawable.shelf_center
            }
            binding.vBottom.background = AppCompatResources.getDrawable(context, drawableRes)
        }
    }
}