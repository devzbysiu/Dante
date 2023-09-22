package dev.zbysiu.homer.camera.overlay

import android.graphics.RectF
import android.util.Size

data class BarcodeObject(
    val barCode: String,
    val boundingBox: RectF,
    val sourceSize: Size,
    val sourceRotationDegrees: Int
)
