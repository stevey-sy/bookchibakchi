package com.example.bookchigibakchigi.ui.addmemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.databinding.ItemTagBinding
import com.example.bookchigibakchigi.model.TagUiModel
import androidx.core.graphics.toColorInt

class TagListAdapter(
    private val onTagDelete: (TagUiModel) -> Unit
) : ListAdapter<TagUiModel, TagListAdapter.TagViewHolder>(TagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding, onTagDelete)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TagViewHolder(
        private val binding: ItemTagBinding,
        private val onTagDelete: (TagUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: TagUiModel) {
            binding.tvTag.apply {
                text = "#${tag.name}"
                setTextColor(tag.textColor.toColorInt())
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    tag.backgroundColor.toColorInt()
                )
                setOnCloseIconClickListener {
                    onTagDelete(tag)
                }
            }
        }
    }

    private class TagDiffCallback : DiffUtil.ItemCallback<TagUiModel>() {
        override fun areItemsTheSame(oldItem: TagUiModel, newItem: TagUiModel): Boolean {
            return oldItem.tagId == newItem.tagId
        }

        override fun areContentsTheSame(oldItem: TagUiModel, newItem: TagUiModel): Boolean {
            return oldItem == newItem
        }
    }
} 