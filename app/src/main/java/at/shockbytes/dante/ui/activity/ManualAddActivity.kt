package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.core.network.DetailsDownloader
import at.shockbytes.dante.core.shortcut.AppShortcutHandler
import at.shockbytes.dante.databinding.ManualAddActivityBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerTintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.ManualAddFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddActivity : ContainerTintableBackNavigableActivity<ManualAddActivityBinding>() {

    @Inject
    lateinit var appShortcutHandler: AppShortcutHandler

    @Inject
    lateinit var detailsDownloader: DetailsDownloader

    private var bookEntity: BookEntity? = null

    override val displayFragment: Fragment
        get() = ManualAddFragment.newInstance(bookEntity)

    override fun onCreate(savedInstanceState: Bundle?) {
        // NOTE: this is a hack:
        // - I need to inject detailsDownloader
        // - it will be injected in `super.onCreate() below, but it's too late
        // - so injectToGraph is called here and later in super.onCreate() as well
        injectToGraph((application as DanteApp).appComponent)
        bookEntity = when (intent?.action) {
            Intent.ACTION_SEND -> {
                val amazonUrl = intent.getStringExtra(Intent.EXTRA_TEXT)
                Timber.d("send: ${intent.getStringExtra(Intent.EXTRA_TEXT)}")
                // TODO: Blocking first?
                detailsDownloader.downloadDetails(amazonUrl!!).blockingFirst()
            }
            else -> intent.extras?.getParcelable(ARG_BOOK_ENTITY_UPDATE)
        }
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