package com.example.bookchigibakchigi.ui.bookdetail.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookchigibakchigi.R
import com.example.bookchigibakchigi.data.PhotoCardWithTextContents
import java.io.File

class PhotoCardAdapter(private var photoCardList: List<PhotoCardWithTextContents>) :
    RecyclerView.Adapter<PhotoCardAdapter.PhotoCardViewHolder>() {

    class PhotoCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.imageViewPhoto)

        fun bind(photoCardWithTextContent: PhotoCardWithTextContents) {
            val context = itemView.context
            val imageFileName = photoCardWithTextContent.photoCard.imageFileName
            val file = File(context.filesDir, imageFileName) // 내부 저장소에서 파일 경로 생성
            val fileUri = Uri.fromFile(file) // Glide에 전달할 URI 생성

            Glide.with(context)
                .load(fileUri)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_card, parent, false)
        return PhotoCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoCardViewHolder, position: Int) {
        holder.bind(photoCardList[position])
    }

    override fun getItemCount(): Int = photoCardList.size

    fun updateList(newList: List<PhotoCardWithTextContents>) {
        photoCardList = newList
        notifyDataSetChanged()
    }
}