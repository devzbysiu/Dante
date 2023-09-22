package dev.zbysiu.homer.core.image

import android.graphics.drawable.Drawable

interface ImageLoadingCallback {

    fun onImageResourceReady(resource: Drawable?)

    fun onImageLoadingFailed(e: Exception?)
}