package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.zbysiu.homer.ui.activity.core.ContainerActivity
import dev.zbysiu.homer.ui.fragment.SuggestionsFragment

class SuggestionsActivity : ContainerActivity() {

    override val displayFragment: Fragment
        get() = SuggestionsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SuggestionsActivity::class.java)
    }
}