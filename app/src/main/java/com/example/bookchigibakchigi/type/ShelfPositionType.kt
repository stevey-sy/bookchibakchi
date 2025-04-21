package com.example.bookchigibakchigi.type

sealed class ShelfPositionType {
    object Start : ShelfPositionType()
    object Center : ShelfPositionType()
    object End : ShelfPositionType()
} 