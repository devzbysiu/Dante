package dev.zbysiu.homer.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.navigation.ActivityNavigator
import dev.zbysiu.homer.navigation.Destination
import dev.zbysiu.homer.navigation.Destination.BookDetail.BookDetailInfo
import dev.zbysiu.homer.ui.adapter.main.BookAdapter
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.ui.activity.ManualAddActivity.Companion.EXTRA_UPDATED_BOOK_STATE
import dev.zbysiu.homer.ui.adapter.OnBookActionClickedListener
import dev.zbysiu.homer.ui.adapter.main.BookAdapterItem
import dev.zbysiu.homer.ui.adapter.main.RandomPickCallback
import dev.zbysiu.homer.ui.fragment.BookDetailFragment.Companion.ACTION_BOOK_CHANGED
import dev.zbysiu.homer.ui.viewmodel.BookListViewModel
import dev.zbysiu.homer.core.Constants.ACTION_BOOK_CREATED
import dev.zbysiu.homer.core.Constants.EXTRA_BOOK_CREATED_STATE
import dev.zbysiu.homer.databinding.FragmentBookMainBinding
import dev.zbysiu.homer.ui.activity.MainActivity
import dev.zbysiu.homer.util.DanteUtils
import dev.zbysiu.homer.ui.view.SharedViewComponents
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.arguments.argument
import dev.zbysiu.homer.util.runDelayed
import dev.zbysiu.homer.util.setVisible
import dev.zbysiu.homer.util.viewModelOf
import dev.zbysiu.tracking.properties.LoginSource
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class MainBookFragment : BaseFragment<FragmentBookMainBinding>(),
    BaseAdapter.OnItemClickListener<BookAdapterItem>,
    BaseAdapter.OnItemMoveListener<BookAdapterItem>,
    OnBookActionClickedListener {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var bookAdapter: BookAdapter
    private lateinit var viewModel: BookListViewModel

    private var bookState: BookState by argument()
    private var allowItemClick: Boolean by argument()

    private val onLabelClickedListener: ((BookLabel) -> Unit) = { label ->
        LabelCategoryBottomSheetFragment.newInstance(label)
            .show(childFragmentManager, "show-label-bottom-sheet")
    }

    private val onBookOverflowClickedListener: ((BookEntity) -> Unit) = { book ->
        BookActionBottomSheetFragment.newInstance(book)
            .show(childFragmentManager, "book-action-bottom-sheet")
    }

    private val dismissWishlistExplanation: () -> Unit = {
        viewModel.dismissWishlistExplanation()
        bookAdapter.deleteEntity(BookAdapterItem.WishlistExplanation)
    }

    private val randomPickCallback = object : RandomPickCallback {
        override fun onDismiss() {
            showToast(R.string.random_pick_restore_instruction)
            viewModel.onDismissRandomBookPicker()
            bookAdapter.deleteEntity(BookAdapterItem.RandomPick)
        }

        override fun onRandomPickClicked() {
            viewModel.pickRandomBookToRead()
        }
    }

    private val bookUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, data: Intent?) {
            when (data?.action) {
                ACTION_BOOK_CHANGED -> handleBookUpdatedBroadcast(data)
                ACTION_BOOK_CREATED -> handleBookCreatedBroadcast(data)
            }
        }
    }

    private fun handleBookUpdatedBroadcast(data: Intent) {
        (data.getSerializableExtra(EXTRA_UPDATED_BOOK_STATE) as? BookState)
            ?.let { updatedBookState ->
                viewModel.onBookUpdatedEvent(updatedBookState)
            }
    }

    private fun handleBookCreatedBroadcast(data: Intent) {
        (data.getSerializableExtra(EXTRA_BOOK_CREATED_STATE) as? BookState)
            ?.let { createdBookState ->
                if (viewModel.state == createdBookState) {
                    runDelayed(500) {
                        // Might be null, there have been reported crashes
                        vb.rvMainBookFragment?.smoothScrollToPosition(0)
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)

        viewModel.state = bookState

        registerBookUpdatedBroadcastReceiver()
    }


    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBookMainBinding {
        return FragmentBookMainBinding.inflate(inflater, root, attachToRoot)
    }

    private fun registerBookUpdatedBroadcastReceiver() {

        val intentFilter = IntentFilter().apply {
            addAction(ACTION_BOOK_CREATED)
            addAction(ACTION_BOOK_CHANGED)
        }

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(bookUpdatedReceiver, intentFilter)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        vb.rvMainBookFragment.suppressLayout(false)
    }

    override fun onPause() {
        super.onPause()
        vb.rvMainBookFragment.suppressLayout(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(bookUpdatedReceiver)
    }

    override fun bindViewModel() {
        viewModel.getBooks().observe(this, Observer(::handleBookLoadingState))

        viewModel.onPickRandomBookEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleRandomPickEvent)
            .addTo(compositeDisposable)

        viewModel.onSuggestionEvent()
            .subscribe(::handleSuggestionEvent)
            .addTo(compositeDisposable)

        viewModel.onEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleEvents)
            .addTo(compositeDisposable)
    }

    private fun handleSuggestionEvent(state: BookListViewModel.SuggestionState) {
        when (state) {
            is BookListViewModel.SuggestionState.Suggest -> {
                showSuggestionBottomSheet(state.book)
            }
            is BookListViewModel.SuggestionState.UserNotLoggedIn -> {
                showSuggestionErrorDialog(
                    icon = R.drawable.ic_user_template_dark,
                    title = R.string.login_required,
                    message = R.string.suggestion_login_required_message,
                    secondaryAction = SecondaryAction(R.string.login) {
                        (activity as? MainActivity)?.forceLogin(LoginSource.FromSuggestion)
                    }
                )
            }
            is BookListViewModel.SuggestionState.WrongLanguage -> {
                showSuggestionErrorDialog(
                    icon = R.drawable.ic_language_english,
                    title = R.string.suggestion_wrong_language_title,
                    message = R.string.suggestion_wrong_language_message
                )
            }
        }
    }

    private fun showSuggestionBottomSheet(book: BookEntity) {
        SuggestBookBottomSheetDialogFragment.newInstance(book)
            .setOnRecommendationEnteredListener { recommendation ->
                viewModel.suggestBook(book, recommendation)
            }
            .show(parentFragmentManager, "suggest-book-fragment")
    }

    private data class SecondaryAction(
        val titleRes: Int,
        val action: () -> Unit
    )

    private fun showSuggestionErrorDialog(
        icon: Int,
        title: Int,
        message: Int,
        secondaryAction: SecondaryAction? = null
    ) {

        MaterialDialog(requireContext()).show {
            icon(icon)
            title(text = getString(title))
            message(text = getString(message))
            positiveButton(android.R.string.ok) {
                dismiss()
            }
            secondaryAction?.let {
                negativeButton(secondaryAction.titleRes) { secondaryAction.action() }
            }
            cancelOnTouchOutside(true)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun handleEvents(event: BookListViewModel.Event) {
        when (event) {
            is BookListViewModel.Event.SuggestionPlaced -> showToast(event.textRes)
        }
    }

    private fun handleBookLoadingState(state: BookListViewModel.BookLoadingState) {

        when (state) {
            is BookListViewModel.BookLoadingState.Success -> {
                vb.tvMainBookFragmentEmpty.setVisible(false)
                vb.rvMainBookFragment.setVisible(true)

                bookAdapter.updateData(state.books)
            }

            is BookListViewModel.BookLoadingState.Empty -> {
                vb.tvMainBookFragmentEmpty.setVisible(true)
                vb.rvMainBookFragment.setVisible(false)
            }

            is BookListViewModel.BookLoadingState.Error -> {
                showSnackbar(getString(R.string.load_error), showLong = true)
                vb.rvMainBookFragment.setVisible(false)
            }
        }
    }

    private fun handleRandomPickEvent(event: BookListViewModel.RandomPickEvent) {

        when (event) {
            is BookListViewModel.RandomPickEvent.RandomPick -> {

                PickRandomBookFragment
                    .newInstance(event.book.title, event.book.normalizedThumbnailUrl)
                    .setOnPickClickListener {
                        viewModel.moveBookToCurrentList(event.book)
                    }
                    .let { fragment ->
                        DanteUtils.addFragmentToActivity(
                            parentFragmentManager,
                            fragment,
                            android.R.id.content,
                            true
                        )
                    }
            }
            BookListViewModel.RandomPickEvent.NoBookAvailable -> {
                Timber.e(IllegalStateException("No book available in random pick event! Should never happen!"))
            }
        }
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {

        vb.tvMainBookFragmentEmpty.text = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]

        bookAdapter = BookAdapter(
            requireContext(),
            imageLoader,
            onOverflowActionClickedListener = onBookOverflowClickedListener,
            onItemClickListener = this,
            onItemMoveListener = this,
            wishlistExplanationDismissListener = dismissWishlistExplanation,
            onLabelClickedListener = onLabelClickedListener,
            randomPickCallback = randomPickCallback
        )

        vb.rvMainBookFragment.apply {
            layoutManager = SharedViewComponents.layoutManagerForBooks(requireContext())
            adapter = bookAdapter
        }

        val itemTouchHelper = ItemTouchHelper(
            BaseItemTouchHelper(
                bookAdapter,
                allowSwipeToDismiss = false,
                BaseItemTouchHelper.DragAccess.VERTICAL
            )
        )
        itemTouchHelper.attachToRecyclerView(vb.rvMainBookFragment)
    }

    override fun onItemClick(content: BookAdapterItem, position: Int, v: View) {
        when (content) {
            is BookAdapterItem.Book -> handleBookClick(content, v)
            BookAdapterItem.RandomPick -> Unit // Do nothing
            BookAdapterItem.WishlistExplanation -> Unit // Do nothing
        }
    }

    private fun handleBookClick(content: BookAdapterItem.Book, v: View) {
        if (allowItemClick) {
            ActivityNavigator.navigateTo(
                context,
                Destination.BookDetail(BookDetailInfo(content.id, content.title)),
                getTransitionBundle(v)
            )
        }
    }

    override fun onItemDismissed(t: BookAdapterItem, position: Int) = Unit

    // Do nothing, only react to move actions in the on item move finished method
    override fun onItemMove(t: BookAdapterItem, from: Int, to: Int) = Unit

    override fun onItemMoveFinished() = viewModel.updateBookPositions(bookAdapter.data)

    override fun onDelete(book: BookEntity, onDeletionConfirmed: (Boolean) -> Unit) {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_delete)
            title(text = getString(R.string.ask_for_book_deletion))
            message(text = getString(R.string.ask_for_book_deletion_msg, book.title))
            positiveButton(R.string.action_delete) {
                onDeletionConfirmed(true)
                viewModel.deleteBook(book)
                bookAdapter.deleteEntity(book.toAdapterEntity())
            }
            negativeButton(android.R.string.cancel) {
                onDeletionConfirmed(false)
                dismiss()
            }
            cancelOnTouchOutside(false)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    override fun onShare(book: BookEntity) {
        ActivityNavigator.navigateTo(context, Destination.Share(book))
    }

    override fun onEdit(book: BookEntity) {
        ActivityNavigator.navigateTo(
            context,
            Destination.ManualAdd(book),
            ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
        )
    }

    override fun onSuggest(book: BookEntity) {
        viewModel.verifyBookSuggestion(book)
    }

    override fun onMoveToUpcoming(book: BookEntity) {
        viewModel.moveBookToUpcomingList(book)
        bookAdapter.deleteEntity(book.toAdapterEntity())
    }

    override fun onMoveToCurrent(book: BookEntity) {
        viewModel.moveBookToCurrentList(book)
        bookAdapter.deleteEntity(book.toAdapterEntity())
    }

    override fun onMoveToDone(book: BookEntity) {
        viewModel.moveBookToDoneList(book)
        bookAdapter.deleteEntity(book.toAdapterEntity())
    }

    private fun getTransitionBundle(v: View): Bundle? {
        return ActivityOptionsCompat
            .makeSceneTransitionAnimation(requireActivity(),
                Pair(
                    v.findViewById(R.id.item_book_card),
                    getString(R.string.transition_detail_screen)
                )
            )
            .toBundle()
    }

    private fun BookEntity.toAdapterEntity(): BookAdapterItem = BookAdapterItem.Book(this)

    companion object {

        fun newInstance(state: BookState, allowItemClick: Boolean = true): MainBookFragment {
            return MainBookFragment().apply {
                this.allowItemClick = allowItemClick
                this.bookState = state
            }
        }
    }
}
