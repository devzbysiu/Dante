package dev.zbysiu.homer.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.core.ui.NegativeDrawable
import dev.zbysiu.homer.databinding.FragmentSuggestBookBinding
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.arguments.argument
import dev.zbysiu.homer.util.isNightModeEnabled
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding2.widget.RxTextView
import javax.inject.Inject

class SuggestBookBottomSheetDialogFragment : BaseBottomSheetFragment<FragmentSuggestBookBinding>() {

    private var bookEntity: BookEntity by argument()

    private var onRecommendationEnteredListener: ((String) -> Unit)? = null

    override fun getTheme() = R.style.BottomSheetDialogTheme

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentSuggestBookBinding {
        return FragmentSuggestBookBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        setBookImageAndTitle()
        setBackgroundImage()
        checkRecommendationInput()
        setupConfirmButtonListener()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    private fun setupConfirmButtonListener() {
        vb.btnSuggestBookConfirm.setOnClickListener {
            vb.editTextEnterSuggestion.text?.toString()?.let { text ->
                onRecommendationEnteredListener?.invoke(text.trim())
                dismiss()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    private fun setBookImageAndTitle() {

        vb.tvSuggestBookTitle.text = bookEntity.title

        val imageUrl = bookEntity.normalizedThumbnailUrl
        if (!imageUrl.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(
                requireContext(),
                imageUrl,
                vb.ivSuggestBookCover,
                cornerDimension = requireContext().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        } else {
            vb.ivSuggestBookCover.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setBackgroundImage() {
        ContextCompat.getDrawable(requireContext(), R.drawable.suggestion_background)?.let {
            val drawable = if (requireContext().isNightModeEnabled()) {
                NegativeDrawable.ofDrawable(it).drawable
            } else it

            vb.ivSuggestionCoverBackground.setImageDrawable(drawable)
        }
    }

    private fun checkRecommendationInput() {
        RxTextView.textChanges(vb.editTextEnterSuggestion)
            .map { text ->
                // Do not allow more than 10 line breaks
                text.count() in 1 until MAX_CHARS && text.count { it == '\n' } < 10
            }
            .subscribe(vb.btnSuggestBookConfirm::setEnabled)
            .addTo(compositeDisposable)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    fun setOnRecommendationEnteredListener(
        function: (String) -> Unit
    ): SuggestBookBottomSheetDialogFragment {
        return this.apply {
            onRecommendationEnteredListener = function
        }
    }

    companion object {

        private const val MAX_CHARS = 180

        fun newInstance(
            bookEntity: BookEntity
        ): SuggestBookBottomSheetDialogFragment {
            return SuggestBookBottomSheetDialogFragment().apply {
                this.bookEntity = bookEntity
            }
        }
    }
}