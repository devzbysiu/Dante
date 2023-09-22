package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import dev.zbysiu.homer.R
import dev.zbysiu.homer.camera.BarcodeScanResultBottomSheetDialogFragment
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.injection.ViewModelFactory
import dev.zbysiu.homer.ui.activity.core.BaseActivity
import dev.zbysiu.homer.ui.fragment.SearchFragment
import dev.zbysiu.homer.ui.viewmodel.SearchViewModel
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.viewModelOf
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
class SearchActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModelOf(vmFactory)
        bindViewModel()

        window.exitTransition = Fade()
        window.enterTransition = Fade()

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SearchFragment.newInstance())
            .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private fun bindViewModel() {

        viewModel.bookDownloadEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { item ->
                    supportActionBar?.show()
                    showDownloadFragment(item.isbn)
                }
                .addTo(compositeDisposable)
    }

    private fun showDownloadFragment(query: String) {
        BarcodeScanResultBottomSheetDialogFragment
            .newInstance(query, askForAnotherScan = false, showNotMyBookButton = false)
            .setOnBookAddedListener { bookTitle ->
                showToast(getString(R.string.book_added_to_library, bookTitle))
                supportFinishAfterTransition()
            }
            .show(supportFragmentManager, "bottom-sheet-add-search")
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }
}