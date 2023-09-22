package dev.zbysiu.homer.ui.adapter.suggestions

import dev.zbysiu.homer.suggestions.Suggestion

interface OnSuggestionActionClickedListener {

    fun onAddSuggestionToWishlist(suggestion: Suggestion)

    fun onReportBookSuggestion(suggestionId: String, suggestionTitle: String)
}