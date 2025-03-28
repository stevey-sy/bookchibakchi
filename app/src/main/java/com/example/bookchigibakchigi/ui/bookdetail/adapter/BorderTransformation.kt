package com.example.bookchigibakchigi.ui.bookdetail.adapter

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class BorderTransformation(
    private val borderWidth: Int,
    private val borderColor: Int
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height

        val borderBitmap = Bitmap.createBitmap(
            width + (borderWidth * 2),
            height + (borderWidth * 2),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(borderBitmap)
        val paint = Paint().apply {
            color = borderColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, borderBitmap.width.toFloat(), borderBitmap.height.toFloat(), paint)

        val matrix = Matrix()
        matrix.postTranslate(borderWidth.toFloat(), borderWidth.toFloat())
        canvas.drawBitmap(toTransform, matrix, null)

        return borderBitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("BorderTransformation(borderWidth=$borderWidth, borderColor=$borderColor)".toByteArray())
    }
} 