package com.example.bookchigibakchigi.util

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest

object PermissionUtil {
    fun checkCameraPermission(context: Context): Boolean {
        val cameraPermission = Manifest.permission.CAMERA
        return context.checkSelfPermission(cameraPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    }
} 