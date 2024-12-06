package com.example.bookchigibakchigi.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsedListInfo(
    val aladinUsed: UsedDetail?,
    val userUsed: UsedDetail?,
    val spaceUsed: UsedDetail?
) : Parcelable
