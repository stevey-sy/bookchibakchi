package com.example.bookchigibakchigi.ui.mylibrary.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.databinding.ItemBookShelfBinding

class BookShelfAdapter(
    private val onItemClick: (BookEntity, Int, View) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<BookShelfAdapter.BookShelfItemViewHolder>() {

    private val dataList = mutableListOf<BookEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookShelfItemViewHolder {
        val binding = ItemBookShelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookShelfItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // 현재 데이터 개수를 기준으로 3의 배수로 맞춤
        val rowCount = Math.ceil(dataList.size / 3.0).toInt() // 필요한 행의 개수 계산
        return maxOf(9, rowCount * 3) // 최소 9개 이상 유지
    }

    override fun onBindViewHolder(holder: BookShelfItemViewHolder, position: Int) {
        if (position < dataList.size) {
            // 실제 데이터가 있는 경우 표시
            holder.bind(dataList[position], position, onItemClick)
        } else {
            // 빈 아이템을 처리
            holder.bindEmpty(position, itemCount)
        }
    }

    fun setDataList(newList: List<BookEntity>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    class BookShelfItemViewHolder(private val binding: ItemBookShelfBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bookEntity: BookEntity, position: Int, onItemClick: (BookEntity, Int, View) -> Unit) {

            binding.ivBook.transitionName = "sharedView_${bookEntity.itemId}"

            binding.root.setOnClickListener{
                onItemClick(bookEntity, position, binding.ivBook)
            }
            // 타입에 따른 뷰 가시성 처리
//            binding.rlPlus.visibility = if (bookEntity.bookType == "0") View.VISIBLE else View.GONE
            // 열 위치에 따라 배경 Drawable 설정
            val context = binding.root.context
            val drawableRes = when (position % 3) {
                0 -> R.drawable.shelf_left // 왼쪽 아이템의 Drawable
                1 -> R.drawable.shelf_center // 가운데 아이템의 Drawable
                2 -> R.drawable.shelf_right // 오른쪽 아이템의 Drawable
                else -> R.drawable.shelf_center // 기본 Drawable (예외 처리용)
            }

            // 배경 Drawable 적용
            binding.vBottom.background = AppCompatResources.getDrawable(context, drawableRes)
            binding.ivBook.visibility = if (bookEntity.bookType == "0") View.GONE else View.VISIBLE
            binding.vBookShadow.visibility = View.VISIBLE
            binding.vBookShadowUp.visibility = View.VISIBLE
            binding.ivPlant.visibility = View.INVISIBLE
            binding.ivClock.visibility = View.INVISIBLE
            binding.ivClock.visibility = View.INVISIBLE
            if (bookEntity.coverImageUrl.isNotEmpty()) {
                binding.ivBook.visibility = View.VISIBLE
                Glide.with(context)
                    .load(bookEntity.coverImageUrl)
                    .into(binding.ivBook)
//                binding.rlPlus.setBackgroundColor(Color.TRANSPARENT)
            } else {
                // 이미지 URL이 비어 있는 경우 기본 이미지로 설정
                binding.ivBook.visibility = View.GONE
            }

        }

        fun bindEmpty(position: Int, itemCount: Int) {

            binding.ivBook.transitionName = "sharedView_"

            binding.ivBook.visibility = View.INVISIBLE
            binding.vBookShadow.visibility = View.INVISIBLE
            binding.vBookShadowUp.visibility = View.INVISIBLE
            binding.ivPlant.visibility = if (position == itemCount - 1) View.VISIBLE else View.INVISIBLE
//            binding.ivClock.visibility = if (position == itemCount - 3) View.VISIBLE else View.INVISIBLE
//            binding.ivFlower.visibility = if (position == itemCount - 5) View.VISIBLE else View.INVISIBLE
            binding.ivClock.visibility = View.INVISIBLE
            binding.ivFlower.visibility = View.INVISIBLE

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