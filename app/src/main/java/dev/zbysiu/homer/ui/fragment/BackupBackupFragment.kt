package dev.zbysiu.homer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import dev.zbysiu.homer.R
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.databinding.FragmentBackupBackupBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.adapter.BackupStorageProviderAdapter
import dev.zbysiu.homer.ui.viewmodel.BackupViewModel
import dev.zbysiu.homer.util.Priority
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.viewModelOfActivity
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.view.EqualSpaceItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupBackupFragment : BaseFragment<FragmentBackupBackupBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBackupBackupBinding {
        return FragmentBackupBackupBinding.inflate(inflater, root, attachToRoot)
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

    override fun setupViews() = Unit

    override fun bindViewModel() {

        viewModel.getLastBackupTime()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lastBackup ->
                vb.tvFragmentBackupLastBackup.text = getString(R.string.last_backup, lastBackup)
            }
            .addTo(compositeDisposable)

        viewModel.getActiveBackupProviders().observe(this, Observer(::setupBackupProviderUI))

        viewModel.makeBackupEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleBackupState)
            .addTo(compositeDisposable)
    }

    private fun handleBackupState(state: BackupViewModel.State) {
        when (state) {
            is BackupViewModel.State.Success -> {
                showSnackbar(getString(R.string.backup_created), showLong = false)

                if (state.switchToBackupTab) {
                    switchToBackupTab()
                }
            }
            is BackupViewModel.State.Error -> {
                showSnackbar(getString(R.string.backup_not_created))
            }
        }
    }

    private fun switchToBackupTab() {
        (parentFragment as? BackupFragment)?.switchToBackupTab()
    }

    override fun unbindViewModel() = Unit

    private fun setupBackupProviderUI(providers: List<BackupStorageProvider>) {

        vb.rvFragmentBackupProviders.apply {
            layoutManager = GridLayoutManager(requireContext(), 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return (vb.rvFragmentBackupProviders.adapter as BackupStorageProviderAdapter)
                            .data[position].priority.run {
                            when (this) {
                                Priority.LOW -> 1
                                Priority.MEDIUM -> 1
                                Priority.HIGH -> 2
                            }
                        }
                    }
                }
            }
            adapter = BackupStorageProviderAdapter(
                requireContext(),
                onItemClickListener = object : BaseAdapter.OnItemClickListener<BackupStorageProvider> {
                    override fun onItemClick(content: BackupStorageProvider, position: Int, v: View) {
                        viewModel.makeBackup(content)
                    }
                }
            ).apply {
                updateData(providers.sortedBy { it.priority })
            }

            if (itemDecorationCount == 0) {
                addItemDecoration(EqualSpaceItemDecoration(context.resources.getDimension(R.dimen.backup_provider_margin).toInt()))
            }
        }
    }

    companion object {

        fun newInstance(): BackupBackupFragment {
            return BackupBackupFragment()
        }
    }
}