package dev.zbysiu.homer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.zbysiu.homer.injection.AppComponent
import androidx.lifecycle.ViewModelProvider
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ActivityBookStorageBinding
import dev.zbysiu.homer.ui.activity.core.BaseBindingActivity
import dev.zbysiu.homer.ui.fragment.BackupFragment
import dev.zbysiu.homer.ui.fragment.ImportBooksStorageFragment
import dev.zbysiu.homer.ui.fragment.OnlineStorageFragment
import javax.inject.Inject
import dev.zbysiu.homer.ui.viewmodel.BackupViewModel
import dev.zbysiu.homer.util.viewModelOf
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

class BookStorageActivity : BaseBindingActivity<ActivityBookStorageBinding>(), PermissionCallbacks {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithBinding(ActivityBookStorageBinding::inflate)
        viewModel = viewModelOf(vmFactory)

        initializeNavigation()
        hideActionBar()
        setupToolbar()
    }

    private fun setupToolbar() {
        vb.toolbarBookStorage.danteToolbarTitle.setText(R.string.label_book_storage)
        vb.toolbarBookStorage.danteToolbarBack.apply {
            setVisible(true)
            setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun initializeNavigation() {

        vb.bottomNavigationBookStorage.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.menu_book_storage_online -> {
                    showFragment(OnlineStorageFragment.newInstance())
                }
                R.id.menu_book_storage_local -> {
                    showFragment(BackupFragment.newInstance())
                }
                R.id.menu_book_storage_import -> {
                    showFragment(ImportBooksStorageFragment.newInstance())
                }
            }

            true
        }

        vb.bottomNavigationBookStorage.selectedItemId = R.id.menu_book_storage_local
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_book_storage, fragment)
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) = Unit

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Reload data sources once external permission is granted
        viewModel.connect(this, forceReload = true)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BookStorageActivity::class.java)
        }
    }
}
