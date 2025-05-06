package com.example.bookchigibakchigi.ui.bookdetail.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.databinding.ItemCardMemoBinding
import com.example.bookchigibakchigi.databinding.ItemMemoBinding
import com.example.bookchigibakchigi.model.MemoUiModel
import java.text.SimpleDateFormat
import java.util.Locale

class MemoListAdapter(
    private val onModifyClicked: (Long) -> Unit,
    private val onDeleteClicked: (Long) -> Unit
) : ListAdapter<MemoUiModel, MemoListAdapter.MemoViewHolder>(MemoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemCardMemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(onModifyClicked, onDeleteClicked, getItem(position))
    }

    class MemoViewHolder(
        private val binding: ItemCardMemoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())

        fun bind(onModifyClicked: (Long) -> Unit, onDeleteClicked: (Long) -> Unit, memo: MemoUiModel) {
            binding.apply {
                tvPageNumber.paintFlags = tvPageNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                tvCreatedAt.text = dateFormat.format(memo.createdAt)
                tvContent.text = memo.content
                tvPageNumber.text = "p.${memo.pageNumber}"
                rlCardBackground.background = AppCompatResources. getDrawable(root.context, memo.background)
                // 태그 처리
                val tagsText = memo.tags.joinToString(" ") { "#${it.name}" }
                if(tagsText.isEmpty()) tvTags.visibility = View.GONE
                tvTags.text = tagsText

                // 수정, 삭제 메뉴 보여주기.
                ivOptions.setOnClickListener {
                    val popupMenu = PopupMenu(root.context, ivOptions) // anchorView는 "..." 버튼
                    popupMenu.menuInflater.inflate(R.menu.menu_card, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.modify -> {
                                onModifyClicked(memo.memoId)
                                true
                            }
                            R.id.delete -> {
                                onDeleteClicked(memo.memoId)
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }

//                // 태그 처리
//                chipGroup.removeAllViews()
//                memo.tags.forEach { tag ->
//                    val chip = com.google.android.material.chip.Chip(chipGroup.context).apply {
//                        text = "#${tag.name}"
//                        isClickable = false
//                        chipBackgroundColor = android.content.res.ColorStateList.valueOf(
//                            android.graphics.Color.parseColor("#F5F5F5")
//                        )
//                        chipStrokeColor = android.content.res.ColorStateList.valueOf(
//                            android.graphics.Color.parseColor("#DDDDDD")
//                        )
//                        chipStrokeWidth = 1f
//                        setTextColor(android.graphics.Color.parseColor("#666666"))
//                        chipCornerRadius = 12f
//                        //typeface = android.graphics.Typeface.createFromAsset(context.assets, "fonts/maruburi_light.ttf")
//                    }
//                    chipGroup.addView(chip)
//                }

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