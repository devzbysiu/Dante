package dev.zbysiu.homer.core.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import android.widget.ImageView
import dev.zbysiu.homer.core.R
import io.reactivex.Single

interface ImageLoader {

    fun loadImage(
        context: Context,
        url: String,
        target: ImageView,
        @DrawableRes placeholder: Int = R.drawable.ic_placeholder,
        circular: Boolean = false,
        callback: ImageLoadingCallback? = null,
        callbackHandleValues: Pair<Boolean, Boolean>? = null
    )

    fun loadImageResource(
        context: Context,
        @DrawableRes resource: Int,
        target: ImageView,
        @DrawableRes placeholder: Int = R.drawable.ic_placeholder,
        circular: Boolean = false,
        callback: ImageLoadingCallback? = null,
        callbackHandleValues: Pair<Boolean, Boolean>? = null
    )

    fun loadImageUri(
        context: Context,
        uri: Uri,
        target: ImageView,
        @DrawableRes placeholder: Int = R.drawable.ic_placeholder,
        circular: Boolean = false,
        callback: ImageLoadingCallback? = null,
        callbackHandleValues: Pair<Boolean, Boolean>? = null
    )

    fun loadImageWithCornerRadius(
        context: Context,
        url: String,
        target: ImageView,
        @DrawableRes placeholder: Int = R.drawable.ic_placeholder,
        @Dimension cornerDimension: Int,
        callback: ImageLoadingCallback? = null,
        callbackHandleValues: Pair<Boolean, Boolean>? = null
    )

    // ------------------------------ Extension functions ------------------------------

    fun Uri.loadBitmap(context: Context): Single<Bitmap>

    fun Uri.loadRoundedBitmap(context: Context): Single<Bitmap>
}