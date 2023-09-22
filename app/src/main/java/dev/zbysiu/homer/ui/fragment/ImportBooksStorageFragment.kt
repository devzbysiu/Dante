package dev.zbysiu.homer.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.FragmentImportBooksStorageBinding
import dev.zbysiu.homer.importer.ImportStats
import dev.zbysiu.homer.importer.Importer
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.ui.adapter.ImporterAdapter
import dev.zbysiu.homer.ui.fragment.dialog.ImportApprovalDialogFragmentWrapper
import dev.zbysiu.homer.ui.viewmodel.ImportBooksStorageViewModel
import dev.zbysiu.homer.util.ExceptionHandlers
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.viewModelOf
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.charset.Charset
import javax.inject.Inject

class ImportBooksStorageFragment : BaseFragment<FragmentImportBooksStorageBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentImportBooksStorageBinding {
        return FragmentImportBooksStorageBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ImportBooksStorageViewModel

    private val importAdapter: ImporterAdapter by lazy {
        ImporterAdapter(requireContext(), Importer.values(), viewModel::startImport)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
        vb.rvFragmentImport.apply {
            adapter = importAdapter
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.getImportState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleImportState, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun handleImportState(importState: ImportBooksStorageViewModel.ImportState) {

        when (importState) {
            is ImportBooksStorageViewModel.ImportState.AskForFile -> {
                openAskForFileActivity(importState.mimeType)
            }
            is ImportBooksStorageViewModel.ImportState.Parsed -> {
                handleParsedState(importState)
            }
            is ImportBooksStorageViewModel.ImportState.Error -> {
                showSnackbar(importState.throwable.toString())
            }
            ImportBooksStorageViewModel.ImportState.Imported -> {
                showSnackbar(getString(R.string.import_success))
                viewModel.confirmImport()
            }

            else -> {}
        }
    }

    private fun handleParsedState(state: ImportBooksStorageViewModel.ImportState.Parsed) {

        when (state.importStats) {
            is ImportStats.Success -> {
                ImportApprovalDialogFragmentWrapper
                    .newInstance(state.providerRes, state.providerIconRes, state.importStats)
                    .setOnApplyListener {
                        viewModel.import()
                    }
                    .setOnDismissListener(viewModel::reset)
                    .show(requireContext())
            }
            ImportStats.NoBooks -> {
                showSnackbar(getString(R.string.import_no_books), getString(R.string.got_it), showIndefinite = true) {
                    viewModel.reset()
                }
            }
        }
    }

    private fun openAskForFileActivity(mimeType: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType(mimeType)
            .addCategory(Intent.CATEGORY_OPENABLE)

        startActivityForResult(intent, REQUEST_SAF)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SAF && resultCode == RESULT_OK && data != null) {
            runBlocking(Dispatchers.Unconfined) {
                data.data?.read(requireContext()).let(viewModel::parseFromString)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    suspend fun Uri.read(context: Context): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(this@read)?.use { stream -> stream.readText() }
            ?: throw IllegalStateException("Could not open $this")
    }

    private fun InputStream.readText(charset: Charset = Charsets.UTF_8): String = readBytes().toString(charset)

    override fun unbindViewModel() = Unit

    companion object {

        private const val REQUEST_SAF = 0x2349

        fun newInstance(): ImportBooksStorageFragment {
            return ImportBooksStorageFragment()
        }
    }
}
