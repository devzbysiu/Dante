package dev.zbysiu.homer.camera

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.zbysiu.homer.camera.databinding.FragmentBarcodeScanBottomSheetBinding
import dev.zbysiu.homer.camera.injection.CameraComponentProvider
import dev.zbysiu.homer.camera.viewmodel.BarcodeResultViewModel
import dev.zbysiu.homer.core.Constants.ACTION_BOOK_CREATED
import dev.zbysiu.homer.core.Constants.EXTRA_BOOK_CREATED_STATE
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookLoadingState
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.core.book.BookSuggestion
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.core.network.BookDownloader
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class BarcodeScanResultBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var isbn: String
    private var askForAnotherScan: Boolean = false
    private var showNotMyBookButton: Boolean = true

    private var closeListener: (() -> Unit)? = null
    private var onBookAddedListener: ((CharSequence) -> Unit)? = null

    private lateinit var viewModel: BarcodeResultViewModel

    private var _binding: FragmentBarcodeScanBottomSheetBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val vb: FragmentBarcodeScanBottomSheetBinding
        get() = _binding!!

    override fun getTheme() = R.style.BottomSheetDialogTheme

    @Inject
    lateinit var booksDownloader: BookDownloader

    @Inject
    lateinit var schedulers: SchedulerFacade

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIntoCameraComponent()

        isbn = arguments?.getString(ARG_BARCODE_ISBN)
            ?: throw IllegalStateException("ISBN argument must be not null!")
        askForAnotherScan = arguments?.getBoolean(ARG_ASK_FOR_ANOTHER_SCAN, false) ?: false
        showNotMyBookButton = arguments?.getBoolean(ARG_SHOW_NOT_MY_BOOK_BUTTON, true) ?: true

        viewModel = BarcodeResultViewModel(booksDownloader, schedulers, bookRepository)
        viewModel.loadBook(isbn)
    }

    private fun injectIntoCameraComponent() {
        CameraComponentProvider.get(requireContext().applicationContext).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeScanBottomSheetBinding.inflate(inflater, container, false)
        return vb.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBookLoadingState().observe(this) {
            when (it) {
                is BookLoadingState.Loading -> showLoadingLayout()
                is BookLoadingState.Error -> showErrorLayout(getString(it.cause))
                is BookLoadingState.Success -> showSuccessLayout(it.bookSuggestion)
            }
        }

        viewModel.onBookStoredEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleBookStoredEvent, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun handleBookStoredEvent(event: BarcodeResultViewModel.BookStoredEvent) {
        when (event) {
            is BarcodeResultViewModel.BookStoredEvent.Success -> {
                if (askForAnotherScan) {
                    showBookStoredDialog(event.title, event.state)
                } else {
                    dismiss()
                    sendCreationBroadcast(event.state)
                }
            }

            is BarcodeResultViewModel.BookStoredEvent.Error -> {
                Toast.makeText(context, event.reason, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendCreationBroadcast(state: BookState) {
        LocalBroadcastManager.getInstance(requireContext())
            .sendBroadcast(
                Intent(ACTION_BOOK_CREATED)
                    .putExtra(EXTRA_BOOK_CREATED_STATE, state)
            )
    }

    private fun showBookStoredDialog(storedBook: String, bookState: BookState) {
        MaterialDialog(requireContext()).show {
            title(text = getString(R.string.book_added_to_library, storedBook))
            message(R.string.scan_another_book)
            positiveButton(R.string.yes) {
                this@BarcodeScanResultBottomSheetDialogFragment.dismiss()
            }
            negativeButton(R.string.no) {
                resetCloseListener()
                activity?.supportFinishAfterTransition()
                sendCreationBroadcast(bookState)
            }
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    /**
     * Reset the close listener because the hosting activity is going to be destroyed anyway. This
     * will prevent the app to reopen the camera and therefore to crash.
     */
    private fun resetCloseListener() {
        closeListener = null
    }

    private fun showSuccessLayout(bookSuggestion: BookSuggestion) {
        vb.layoutBarcodeResultError.setVisible(false)
        vb.pbBarcodeResult.setVisible(false)
        vb.groupBarcodeResult.setVisible(true)
        vb.btnBarcodeResultNotMyBook.setVisible(showNotMyBookButton)

        bookSuggestion.mainSuggestion?.run {
            vb.tvBarcodeResultTitle.text = title
            vb.tvBarcodeResultAuthor.text = author

            thumbnailAddress?.let { imageUrl ->
                imageLoader.loadImageWithCornerRadius(
                    requireContext(),
                    imageUrl,
                    vb.ivBarcodeScanResultCover,
                    cornerDimension = AppUtils.convertDpInPixel(6, requireContext())
                )
            }

            vb.btnBarcodeResultForLater.setOnClickListener {
                if (bookRepository.allBooks.any { it.title == this.title }) {
                    Timber.w("Book is already on the list")
                    Toast.makeText(context, "Book is already on your lists!", Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                viewModel.storeBook(this, state = BookState.READ_LATER)
                onBookAddedListener?.invoke(title)
            }

            vb.btnBarcodeResultReading.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READING)
                onBookAddedListener?.invoke(title)
            }

            vb.btnBarcodeResultWishlist.setOnClickListener {
                viewModel.storeBook(this, state = BookState.WISHLIST)
                onBookAddedListener?.invoke(title)
            }

            vb.btnBarcodeResultRead.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READ)
                onBookAddedListener?.invoke(title)
            }
        }

        vb.btnBarcodeResultNotMyBook.setOnClickListener {
            showOtherSuggestionsModal(bookSuggestion.otherSuggestions) { selectedBook ->
                viewModel.setSelectedBook(bookSuggestion, selectedBook)
            }
        }
    }

    private fun showOtherSuggestionsModal(
        suggestions: List<BookEntity>,
        selectionListener: (BookEntity) -> Unit
    ) {

        MaterialDialog(requireContext()).show {
            title(R.string.download_suggestion_header_other)
            customListAdapter(
                BookSuggestionPickerAdapter(
                    requireContext(),
                    suggestions,
                    imageLoader,
                    onItemClickListener = object : BaseAdapter.OnItemClickListener<BookEntity> {
                        override fun onItemClick(content: BookEntity, position: Int, v: View) {
                            selectionListener(content)
                            dismiss()
                        }
                    }
                )
            )
            positiveButton(R.string.nope) {
                dismiss()
            }
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun showErrorLayout(cause: String) {
        vb.pbBarcodeResult.setVisible(false)
        vb.groupBarcodeResult.setVisible(false)
        vb.layoutBarcodeResultError.setVisible(true)
        vb.btnBarcodeResultNotMyBook.setVisible(false)

        vb.tvBarcodeResultErrorCause.text = cause
        vb.btnBarcodeResultErrorClose.setOnClickListener {
            dismiss()
        }
    }

    private fun showLoadingLayout() {
        vb.pbBarcodeResult.setVisible(true)
        vb.groupBarcodeResult.setVisible(false)
        vb.layoutBarcodeResultError.setVisible(false)
        vb.btnBarcodeResultNotMyBook.setVisible(false)
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

    fun setOnBookAddedListener(function: (CharSequence) -> Unit): BarcodeScanResultBottomSheetDialogFragment {
        return this.apply {
            onBookAddedListener = function
        }
    }

    companion object {

        private const val ARG_BARCODE_ISBN = "arg_barcode_isbn"
        private const val ARG_ASK_FOR_ANOTHER_SCAN = "arg_ask_for_another_scan"
        private const val ARG_SHOW_NOT_MY_BOOK_BUTTON = "arg_show_not_my_book_button"

        fun newInstance(
            isbn: String,
            askForAnotherScan: Boolean,
            showNotMyBookButton: Boolean = true
        ): BarcodeScanResultBottomSheetDialogFragment {
            return BarcodeScanResultBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BARCODE_ISBN, isbn)
                    putBoolean(ARG_ASK_FOR_ANOTHER_SCAN, askForAnotherScan)
                    putBoolean(ARG_SHOW_NOT_MY_BOOK_BUTTON, showNotMyBookButton)
                }
            }
        }
    }
}