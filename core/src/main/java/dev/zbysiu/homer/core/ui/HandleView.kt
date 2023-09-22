package dev.zbysiu.homer.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import dev.zbysiu.homer.core.R

class HandleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.handle_view, this)
    }
}