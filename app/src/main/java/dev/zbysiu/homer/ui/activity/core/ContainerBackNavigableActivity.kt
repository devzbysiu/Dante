package dev.zbysiu.homer.ui.activity.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import dev.zbysiu.homer.injection.AppComponent

/**
 * Author:  Martin Macheiner
 * Date:    23.12.2017
 */
abstract class ContainerBackNavigableActivity<V: ViewBinding> : BackNavigableActivity<V>() {

    abstract val displayFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
}