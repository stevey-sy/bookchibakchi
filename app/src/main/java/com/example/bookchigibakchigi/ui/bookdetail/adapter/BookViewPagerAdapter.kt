package com.example.bookchigibakchigi.ui.bookdetail.adapter

import android.R
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.bookchigibakchigi.model.BookUiModel
import com.example.bookchigibakchigi.databinding.ItemViewPagerBinding


class BookViewPagerAdapter(
    private val transitionName: String,
    private val onItemClick: (BookUiModel, Int, View) -> Unit,
    private val onImageLoaded: () -> Unit
) : RecyclerView.Adapter<BookViewPagerAdapter.BookViewPagerViewHolder>() {

    private val dataList = mutableListOf<BookUiModel>()
    private val loadedImages = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewPagerViewHolder {
        val binding = ItemViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewPagerViewHolder(binding, transitionName, onImageLoaded)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: BookViewPagerViewHolder, position: Int) {
        holder.itemView.tag = "page_$position"
        holder.bind(dataList[position])
        holder.itemView.setOnClickListener {
            onItemClick(dataList[position], position, holder.itemView)
        }
    }

    fun setDataList(newList: List<BookUiModel>) {
        dataList.clear()
        dataList.addAll(newList)
        loadedImages.clear()
        notifyDataSetChanged()
    }

    private fun checkImageLoaded(imageUrl: String) {
        loadedImages.add(imageUrl)
        val totalImages = dataList.count { it.coverImageUrl.isNotEmpty() }
        if (loadedImages.size == totalImages && totalImages > 0) {
            onImageLoaded()
        }
    }

    class BookViewPagerViewHolder(
        val binding: ItemViewPagerBinding,
        private val transitionName: String,
        private val onImageLoaded: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookUiModel: BookUiModel) {
            val currentTransitionName = "sharedView_${bookUiModel.itemId}"
            Log.d("viewPager TEST ", currentTransitionName)
            binding.cardView.transitionName = currentTransitionName
            if (bookUiModel.coverImageUrl.isNotEmpty()) {
                binding.ivBook.visibility = View.VISIBLE
                Glide.with(binding.ivBook.context)
                    .load(bookUiModel.coverImageUrl)
                    .transform(RoundedCorners(16))
                    .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (transitionName == currentTransitionName) {
                                onImageLoaded()
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (transitionName == currentTransitionName) {
                                onImageLoaded()
                            }
                            return false
                        }
                    })
                    .into(binding.ivBook)
            }
        }
    }
}