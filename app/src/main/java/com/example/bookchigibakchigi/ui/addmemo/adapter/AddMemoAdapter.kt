package com.example.bookchigibakchigi.ui.addmemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchigibakchigi.databinding.FragmentAddMemoPageBinding
import com.example.bookchigibakchigi.databinding.FragmentAddMemoQuoteBinding
import com.example.bookchigibakchigi.databinding.FragmentAddMemoTagBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddMemoAdapter(
    private val onPageChanged: (String) -> Unit,
    private val onContentChanged: (String) -> Unit,
    private val onTagAdded: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val PAGE_PAGE = 0
        const val PAGE_QUOTE = 1
        const val PAGE_TAG = 2
    }

    private var pageDebounceJob: Job? = null
    private var contentDebounceJob: Job? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PAGE_PAGE -> {
                val binding = FragmentAddMemoPageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PageViewHolder(binding)
            }
            PAGE_QUOTE -> {
                val binding = FragmentAddMemoQuoteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                QuoteViewHolder(binding)
            }
            PAGE_TAG -> {
                val binding = FragmentAddMemoTagBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TagViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PageViewHolder -> holder.bind(onPageChanged)
            is QuoteViewHolder -> holder.bind(onContentChanged)
            is TagViewHolder -> holder.bind(onTagAdded)
        }
    }

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PAGE_PAGE
            1 -> PAGE_QUOTE
            2 -> PAGE_TAG
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    inner class PageViewHolder(private val binding: FragmentAddMemoPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onPageChanged: (String) -> Unit) {
            binding.etPage.doAfterTextChanged { text ->
                pageDebounceJob?.cancel()
                pageDebounceJob = kotlinx.coroutines.MainScope().launch {
                    delay(300)
                    onPageChanged(text.toString())
                }
            }
        }
    }

    inner class QuoteViewHolder(private val binding: FragmentAddMemoQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onContentChanged: (String) -> Unit) {
            binding.etContent.doAfterTextChanged { text ->
                contentDebounceJob?.cancel()
                contentDebounceJob = kotlinx.coroutines.MainScope().launch {
                    delay(300)
                    onContentChanged(text.toString())
                }
            }
        }
    }

    inner class TagViewHolder(private val binding: FragmentAddMemoTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onTagAdded: (String) -> Unit) {
            binding.etTag.doAfterTextChanged { text ->
                // 태그 입력 관련 처리
            }

            binding.btnAddTag.setOnClickListener {
                val tagName = binding.etTag.text.toString().trim()
                if (tagName.isNotEmpty()) {
                    onTagAdded(tagName)
                    binding.etTag.text.clear()
                }
            }
        }
    }
} 