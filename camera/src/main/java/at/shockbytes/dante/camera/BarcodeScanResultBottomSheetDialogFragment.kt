package at.shockbytes.dante.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import at.shockbytes.dante.camera.injection.DaggerCameraComponent
import at.shockbytes.dante.camera.viewmodel.BarcodeResultViewModel
import at.shockbytes.dante.core.book.BookLoadingState
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.book.BookSuggestion
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.injection.CoreInjectHelper
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_barcode_scan_bottom_sheet.*
import timber.log.Timber
import javax.inject.Inject

class BarcodeScanResultBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var isbn: String

    private var closeListener: (() -> Unit)? = null

    private lateinit var viewModel: BarcodeResultViewModel

    @Inject
    lateinit var booksDownloader: BookDownloader

    @Inject
    lateinit var schedulers: SchedulerFacade

    @Inject
    lateinit var bookDao: BookEntityDao

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIntoCameraComponent()

        isbn = arguments?.getString(ARG_BARCODE_ISBN) ?: throw IllegalStateException("ISBN argument must be not null!")
        viewModel = BarcodeResultViewModel(booksDownloader, schedulers, bookDao)
        viewModel.loadBook(isbn)
    }

    private fun injectIntoCameraComponent() {
        DaggerCameraComponent
            .builder()
            .coreComponent(CoreInjectHelper.provideCoreComponent(requireActivity().applicationContext))
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barcode_scan_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        btn_barcode_result_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBookLoadingState().observe(this, Observer { state ->
            when (state) {
                is BookLoadingState.Loading -> showLoadingLayout()
                is BookLoadingState.Error -> showErrorLayout()
                is BookLoadingState.Success -> showSuccessLayout(state.bookSuggestion)
            }
        })

        viewModel.onBookStoredEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ storedBook ->
                showBookStoredDialog(storedBook)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun showBookStoredDialog(storedBook: String) {
        MaterialDialog(requireContext()).show {
            title(text = getString(R.string.book_added_to_library, storedBook))
            message(R.string.scan_another_book)
            positiveButton(R.string.yes) {
                this@BarcodeScanResultBottomSheetDialogFragment.dismiss()
            }
            negativeButton(R.string.no) {
                activity?.supportFinishAfterTransition()
            }
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun showSuccessLayout(bookSuggestion: BookSuggestion) {
        pb_barcode_result.setVisible(false)
        group_barcode_result.setVisible(true)

        bookSuggestion.mainSuggestion?.run {
            tv_barcode_result_title.text = title
            tv_barcode_result_author.text = author

            thumbnailAddress?.let { imageUrl ->
                imageLoader.loadImageWithCornerRadius(
                    requireContext(),
                    imageUrl,
                    iv_barcode_scan_result_cover,
                    cornerDimension = AppUtils.convertDpInPixel(6, requireContext())
                )
            }

            btn_barcode_result_for_later.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READ_LATER)
            }

            btn_barcode_result_reading.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READING)
            }

            btn_barcode_result_read.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READ)
            }
        }

        btn_barcode_result_not_my_book.setOnClickListener {
            // TODO implement choose other books
            Toast.makeText(requireContext(), "Coming soon...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorLayout() {
        // TODO implement error state
    }

    private fun showLoadingLayout() {
        pb_barcode_result.setVisible(true)
        group_barcode_result.setVisible(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent.parent.parent as View).fitsSystemWindows = false
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        closeListener?.invoke()
        super.onDestroy()
    }

    fun setOnCloseListener(function: () -> Unit): BarcodeScanResultBottomSheetDialogFragment {
        return this.apply {
            closeListener = function
        }
    }

    companion object {

        private const val ARG_BARCODE_ISBN = "arg_barcode_isbn"

        fun newInstance(isbn: String): BarcodeScanResultBottomSheetDialogFragment {
            return BarcodeScanResultBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BARCODE_ISBN, isbn)
                }
            }
        }
    }
}