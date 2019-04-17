package at.shockbytes.dante.ui.activity.core

import android.os.Bundle
import androidx.annotation.DrawableRes
import android.view.MenuItem

abstract class BackNavigableActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            back()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        back()
    }

    private fun back() {
        backwardAnimation()
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            onBackStackPopped()
        } else {
            super.onBackPressed()
        }
    }

    open fun backwardAnimation() { }

    open fun onBackStackPopped() { }

    fun setHomeAsUpIndicator(@DrawableRes indicator: Int) {
        supportActionBar?.setHomeAsUpIndicator(indicator)
    }
}
