package com.example.bookchigibakchigi.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.os.Environment

object FileUtil {
    fun createImageFile(context: Context): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
} 