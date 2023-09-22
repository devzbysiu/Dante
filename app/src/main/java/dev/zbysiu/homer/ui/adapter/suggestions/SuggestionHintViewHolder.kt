package dev.zbysiu.homer.ui.adapter.suggestions

import android.view.ViewGroup
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ItemGenericExplanationBinding
import dev.zbysiu.homer.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter

class SuggestionHintViewHolder(
    private val vb: ItemGenericExplanationBinding,
    private val onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(vb.root) {

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {

        vb.ivItemGenericExplanationDismiss.setOnClickListener {
            onSuggestionExplanationClickedListener.onDismissClicked()
        }

        vb.tvItemGenericExplanation.setText(R.string.suggestions_hint)

        vb.ivItemGenericExplanationDecorationStart.setImageResource(R.drawable.ic_suggestions)
        vb.ivItemGenericExplanationDecorationEnd.setImageResource(R.drawable.ic_suggestions)
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
        ): SuggestionHintViewHolder {
            return SuggestionHintViewHolder(
                ItemGenericExplanationBinding.inflate(parent.context.layoutInflater(), parent, false),
                onSuggestionExplanationClickedListener
            )
        }
    }
}
