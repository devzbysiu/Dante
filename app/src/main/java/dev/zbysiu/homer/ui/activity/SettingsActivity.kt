package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import androidx.viewbinding.ViewBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.activity.core.ContainerBackNavigableActivity
import dev.zbysiu.homer.ui.fragment.SettingsFragment

class SettingsActivity<V: ViewBinding> : ContainerBackNavigableActivity<V>() {

    override val displayFragment = SettingsFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}
