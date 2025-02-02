package dev.zbysiu.homer.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.zbysiu.homer.R
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.databinding.FragmentBackupRestoreBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.adapter.BackupEntryAdapter
import dev.zbysiu.homer.ui.adapter.OnBackupOverflowItemListener
import dev.zbysiu.homer.ui.fragment.dialog.RestoreStrategyDialogFragmentWrapper
import dev.zbysiu.homer.ui.viewmodel.BackupViewModel
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.isPortrait
import dev.zbysiu.homer.util.setVisible
import dev.zbysiu.homer.util.viewModelOfActivity
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.view.EqualSpaceItemDecoration
import com.google.android.gms.common.api.ApiException
import dev.zbysiu.homer.util.openFile
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupRestoreFragment : BaseFragment<FragmentBackupRestoreBinding>(), BaseAdapter.OnItemClickListener<BackupMetadataState> {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    private val entryAdapter: BackupEntryAdapter by lazy {
        BackupEntryAdapter(
            requireContext(),
            onItemClickListener = this,
            onItemOverflowMenuClickedListener = object : OnBackupOverflowItemListener {
                override fun onBackupItemDeleted(content: BackupMetadata, location: Int) {
                    onItemDismissed(content, location)
                }

                override fun onBackupItemDownloadRequest(content: BackupMetadata.WithLocalFile) {
                    // Not implemented yet...
                }

                override fun onBackupItemOpenFileRequest(content: BackupMetadata.WithLocalFile) {
                    openFile(content)
                    viewModel.trackOpenFileEvent(content.storageProvider)
                }
            }
        )
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(requireActivity(), vmFactory)
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBackupRestoreBinding {
        return FragmentBackupRestoreBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        vb.rvFragmentBackupRestore.apply {
            layoutManager = getLayoutManagerForAdapter()
            adapter = entryAdapter
            addItemDecoration(EqualSpaceItemDecoration(16))
        }
    }

    private fun getLayoutManagerForAdapter(): RecyclerView.LayoutManager {
        return if (isPortrait()) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }
    }

    override fun onItemClick(content: BackupMetadataState, position: Int, v: View) {
        when (content) {
            is BackupMetadataState.Active -> showBackupRestoreStrategyModal(content)
            is BackupMetadataState.Inactive -> Unit // This state is not supported yet
        }
    }

    override fun bindViewModel() {

        viewModel.getBackupState().observe(this, { state ->

            when (state) {
                is BackupViewModel.LoadBackupState.Success -> {
                    entryAdapter.updateData(state.backups)
                    vb.rvFragmentBackupRestore.smoothScrollToPosition(0)

                    showLoadingView(false)
                    showEmptyStateView(false)
                    showRecyclerView(true)
                }
                is BackupViewModel.LoadBackupState.Empty -> {
                    showLoadingView(false)
                    showEmptyStateView(true)
                    showRecyclerView(false)
                }
                is BackupViewModel.LoadBackupState.Loading -> {
                    showLoadingView(true)
                    showEmptyStateView(false)
                    showRecyclerView(false)
                }
                is BackupViewModel.LoadBackupState.Error -> {
                    showSnackbar(state.throwable.localizedMessage ?: "", showLong = true)
                    showLoadingView(false)
                    showEmptyStateView(false)
                    showRecyclerView(false)
                }
            }
        })

        viewModel.applyBackupEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                when (state) {
                    is BackupViewModel.ApplyBackupState.Success -> {
                        showSnackbar(getString(R.string.backup_restored, state.msg))
                    }
                    is BackupViewModel.ApplyBackupState.Error -> {
                        showSnackbar(getString(R.string.backup_restore_error, state.throwable.localizedMessage), showLong = true)
                    }
                }
            }
            .addTo(compositeDisposable)

        viewModel.deleteBackupEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                when (state) {
                    is BackupViewModel.DeleteBackupState.Success -> {
                        val adapter = vb.rvFragmentBackupRestore.adapter as BackupEntryAdapter
                        adapter.deleteEntity(state.deleteIndex)
                        showSnackbar(getString(R.string.backup_removed))

                        showEmptyStateView(state.isBackupListEmpty)
                    }
                    is BackupViewModel.DeleteBackupState.Error -> {
                        showSnackbar(getErrorMessage(state.throwable))
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    override fun unbindViewModel() = Unit

    private fun onItemDismissed(t: BackupMetadata, position: Int) {
        val currentItems = vb.rvFragmentBackupRestore.adapter?.itemCount ?: -1
        viewModel.deleteItem(t, position, currentItems)
    }

    /**
     * IMPORTANT: This implementation does not work on all devices...
     * TODO: Use FileProvider implementation instead
     */
    private fun openFile(content: BackupMetadata.WithLocalFile) {
        with(requireContext()) {
            Intent
                .createChooser(
                    openFile(content.localFilePath, content.mimeType),
                    resources.getText(R.string.open_backup_file)
                )
                .let(::startActivity)
        }
    }

    private fun showLoadingView(show: Boolean) {
        vb.pbFragmentBackupRestore.setVisible(show)
    }

    private fun showEmptyStateView(show: Boolean) {
        vb.viewFragmentBackupRestoreEmpty.setVisible(show)
    }

    private fun showRecyclerView(show: Boolean) {
        vb.rvFragmentBackupRestore.setVisible(show)
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> getString(R.string.error_msg_execution_exception)
            else -> getString(R.string.error_msg_unknown)
        }
    }

    private fun showBackupRestoreStrategyModal(state: BackupMetadataState.Active) {
        RestoreStrategyDialogFragmentWrapper
            .newInstance()
            .setOnRestoreStrategySelectedListener { strategy ->
                viewModel.applyBackup(state.entry, strategy)
            }
            .show(requireContext())
    }

    companion object {

        fun newInstance(): BackupRestoreFragment {
            return BackupRestoreFragment()
        }
    }
}