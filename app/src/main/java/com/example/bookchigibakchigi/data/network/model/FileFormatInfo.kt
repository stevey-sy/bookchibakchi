package com.example.bookchigibakchigi.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileFormatInfo(
    val fileType: String,
    val fileSize: Int
) : Parcelable
