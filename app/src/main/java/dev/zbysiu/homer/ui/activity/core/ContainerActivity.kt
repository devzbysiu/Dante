package dev.zbysiu.homer.ui.activity.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.zbysiu.homer.injection.AppComponent

abstract class ContainerActivity : BaseActivity() {

    abstract val displayFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
}