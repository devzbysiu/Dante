package dev.zbysiu.homer.ui.custom.profile

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ProfileHeaderViewBinding
import dev.zbysiu.homer.util.layoutInflater

class ProfileHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val vb: ProfileHeaderViewBinding
        get() = ProfileHeaderViewBinding.inflate(context.layoutInflater(), this, true)

    val imageView: ImageView
        get() = vb.ivProfileUser

    fun setUser(name: String?, mailAddress: String?) {
        val userName = if (name.isNullOrEmpty()) context.getString(R.string.anonymous_user) else name
        vb.tvProfileUserName.text = userName
        vb.tvProfileMailAddress.text = mailAddress
    }

    fun reset() {
        setUser("", "")
        imageView.setImageResource(0)
    }
}