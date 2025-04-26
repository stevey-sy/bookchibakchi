package com.example.bookchigibakchigi.ui.bookdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.databinding.ItemMemoBinding
import com.example.bookchigibakchigi.model.MemoUiModel
import java.text.SimpleDateFormat
import java.util.Locale

class MemoListAdapter : ListAdapter<MemoUiModel, MemoListAdapter.MemoViewHolder>(MemoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemMemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MemoViewHolder(
        private val binding: ItemMemoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())

        fun bind(memo: MemoUiModel) {
            binding.apply {
                tvCreatedAt.text = dateFormat.format(memo.createdAt)
                tvContent.text = memo.content
                tvPageNumber.text = "P.${memo.pageNumber}"

                // 태그 처리
                chipGroup.removeAllViews()
                memo.tags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(chipGroup.context).apply {
                        text = tag.name
                        isClickable = false
                    }
                    chipGroup.addView(chip)
                }
            }
        }
    }

    private class MemoDiffCallback : DiffUtil.ItemCallback<MemoUiModel>() {
        override fun areItemsTheSame(oldItem: MemoUiModel, newItem: MemoUiModel): Boolean {
            return oldItem.memoId == newItem.memoId
        }

        override fun areContentsTheSame(oldItem: MemoUiModel, newItem: MemoUiModel): Boolean {
            return oldItem == newItem
        }
    }
} 