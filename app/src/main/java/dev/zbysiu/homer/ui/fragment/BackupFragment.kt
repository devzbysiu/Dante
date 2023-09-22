package dev.zbysiu.homer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.FragmentBackupBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.adapter.BackupPagerAdapter
import dev.zbysiu.homer.ui.viewmodel.BackupViewModel
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.viewModelOfActivity
import com.google.android.gms.common.api.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupFragment : BaseFragment<FragmentBackupBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBackupBinding {
        return FragmentBackupBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(requireActivity(), vmFactory)
    }

    override fun setupViews() {
        setupViewPager()

        vb.tabsFragmentBackup.setupWithViewPager(vb.vpFragmentBackup)
    }

    override fun bindViewModel() {

        viewModel.errorSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ error ->
                showToast(getString(R.string.backup_connection_establish_error, getErrorMessage(error)), showLong = true)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)

        viewModel.connect(requireActivity())
    }

    override fun unbindViewModel() {
        viewModel.disconnect()
    }

    private fun setupViewPager() {
        val pagerAdapter = BackupPagerAdapter(requireContext(), childFragmentManager)

        vb.vpFragmentBackup.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2
        }
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> getString(R.string.error_msg_execution_exception)
            else -> throwable.localizedMessage ?: ""
        }
    }

    fun switchToBackupTab() {
        vb.vpFragmentBackup.setCurrentItem(1, true)
    }

    companion object {

        fun newInstance(): BackupFragment {
            return BackupFragment().apply {
                this.arguments = Bundle().apply {
                }
            }
        }
    }
}