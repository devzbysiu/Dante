package dev.zbysiu.homer.ui.adapter.suggestions

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.ItemSuggestionBinding
import dev.zbysiu.homer.suggestions.BookSuggestionEntity
import dev.zbysiu.homer.suggestions.Suggester
import dev.zbysiu.homer.suggestions.Suggestion
import dev.zbysiu.homer.util.layoutInflater
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter

class SuggestionViewHolder(
    private val vb: ItemSuggestionBinding,
    private val imageLoader: ImageLoader,
    private val onSuggestionActionClickedListener: OnSuggestionActionClickedListener
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(vb.root) {

    private fun context(): Context = vb.root.context

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {
        with((content as SuggestionsAdapterItem.SuggestedBook).suggestion) {
            setupOverflowMenu(suggestionId, suggestion.title)
            setupBook(suggestion)
            setupSuggester(suggester)
            setupRecommendation(recommendation)
            setupBookActionListener(this)
        }
    }

    private fun setupOverflowMenu(suggestionId: String, suggestionTitle: String) {
        vb.ivItemSuggestionReport.setOnClickListener {
            onSuggestionActionClickedListener.onReportBookSuggestion(suggestionId, suggestionTitle)
        }
    }

    private fun setupBook(suggestion: BookSuggestionEntity) {
        vb.tvItemSuggestionAuthor.text = suggestion.author
        vb.tvItemSuggestionTitle.text = suggestion.title
        setThumbnailToView(
            suggestion.thumbnailAddress,
            vb.ivItemSuggestionCover,
            context().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
        )
    }

    private fun setupSuggester(suggester: Suggester) {
        vb.tvItemSuggestionSuggester.text = context().getString(R.string.suggestion_suggester, suggester.name)
        setThumbnailToView(
            suggester.picture,
            vb.ivItemSuggestionSuggester,
            AppUtils.convertDpInPixel(24, context())
        )
    }

    private fun setupRecommendation(recommendation: String) {
        vb.tvItemSuggestionRecommendation.text = recommendation
    }

    private fun setupBookActionListener(suggestion: Suggestion) {
        vb.btnItemSuggestionAdd.setOnClickListener {
            onSuggestionActionClickedListener.onAddSuggestionToWishlist(suggestion)
        }
    }

    private fun setThumbnailToView(
        url: String?,
        view: ImageView,
        radius: Int
    ) {
        if (!url.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(context(), url, view, cornerDimension = radius)
        } else {
            // Books with no image will recycle another cover if not cleared here
            view.setImageResource(R.drawable.ic_placeholder)
        }
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            imageLoader: ImageLoader,
            onSuggestionActionClickedListener: OnSuggestionActionClickedListener
        ): SuggestionViewHolder {
            return SuggestionViewHolder(
                ItemSuggestionBinding.inflate(parent.context.layoutInflater(), parent, false),
                imageLoader,
                onSuggestionActionClickedListener
            )
        }
    }
}
