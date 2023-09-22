package dev.zbysiu.homer.suggestions

data class SuggestionRequest(
    val suggestion: BookSuggestionEntity,
    val recommendation: String
)