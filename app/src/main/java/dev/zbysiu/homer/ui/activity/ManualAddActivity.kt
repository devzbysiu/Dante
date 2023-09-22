package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.shortcut.AppShortcutHandler
import dev.zbysiu.homer.databinding.ManualAddActivityBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.activity.core.ContainerTintableBackNavigableActivity
import dev.zbysiu.homer.ui.fragment.ManualAddFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddActivity : ContainerTintableBackNavigableActivity<ManualAddActivityBinding>() {

    @Inject
    lateinit var appShortcutHandler: AppShortcutHandler

    private var bookEntity: BookEntity? = null

    override val displayFragment: Fragment
        get() = ManualAddFragment.newInstance(bookEntity)

    override fun onCreate(savedInstanceState: Bundle?) {
        bookEntity = intent.extras?.getParcelable(ARG_BOOK_ENTITY_UPDATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_add_activity)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        appShortcutHandler.handleAppShortcutForActivity(
            activity = this,
            shortcutTitle = "extra_app_shortcut_manual_add",
            action = {
                Timber.d("Coming from app shortcut. Do nothing here.")
            }
        )
    }

    companion object {

        const val EXTRA_UPDATED_BOOK_STATE = "extra_updated_book_state"

        private const val ARG_BOOK_ENTITY_UPDATE = "arg_book_entity_update"

        fun newIntent(context: Context, bookEntity: BookEntity? = null): Intent {
            return Intent(context, ManualAddActivity::class.java)
                .putExtra(ARG_BOOK_ENTITY_UPDATE, bookEntity)
        }
    }
}