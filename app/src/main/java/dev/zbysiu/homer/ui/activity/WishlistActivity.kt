package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.databinding.ActivityWishlistBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.activity.core.BaseBindingActivity
import dev.zbysiu.homer.ui.fragment.MainBookFragment
import dev.zbysiu.homer.util.setVisible

class WishlistActivity : BaseBindingActivity<ActivityWishlistBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithBinding(ActivityWishlistBinding::inflate)
        supportActionBar?.hide()

        setupToolbar()

        supportFragmentManager.beginTransaction()
            .replace(
                vb.wishlistFragmentPlaceholder.id,
                MainBookFragment.newInstance(BookState.WISHLIST)
            )
            .commit()
    }

    private fun setupToolbar() {
        with(vb.toolbarWishlist) {
            danteToolbarTitle.setText(R.string.wishlist_title)
            danteToolbarBack.apply {
                setVisible(true)
                setOnClickListener {
                    onBackPressed()
                }
            }
        }
    }


    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, WishlistActivity::class.java)
    }
}