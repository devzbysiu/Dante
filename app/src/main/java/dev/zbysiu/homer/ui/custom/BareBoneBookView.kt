package dev.zbysiu.homer.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import dev.zbysiu.homer.databinding.BareBoneBookViewBinding
import dev.zbysiu.homer.util.layoutInflater

class BareBoneBookView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val vb = BareBoneBookViewBinding.inflate(context.layoutInflater(), this, true)

    val imageView: ImageView
        get() = vb.ivBareBoneBookView

    fun setTitle(title: String) {
        vb.tvBareBoneBookView.text = title
    }
}