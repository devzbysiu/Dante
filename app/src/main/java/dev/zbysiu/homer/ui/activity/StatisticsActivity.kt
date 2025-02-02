package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.zbysiu.homer.ui.activity.core.ContainerActivity
import dev.zbysiu.homer.ui.fragment.StatisticsFragment

class StatisticsActivity : ContainerActivity() {

    override val displayFragment = StatisticsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, StatisticsActivity::class.java)
    }
}