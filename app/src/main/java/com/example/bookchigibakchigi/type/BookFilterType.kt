package com.example.bookchigibakchigi.type

sealed class BookFilterType {
    object Reading : BookFilterType()
    object Finished : BookFilterType()
    object All : BookFilterType()
} 