package dev.zbysiu.homer.suggestions.firebase

data class FirebaseSuggestions(
    val suggestions: List<FirebaseSuggestion>
) {

    data class FirebaseSuggestion(
        val title: String
    )
}