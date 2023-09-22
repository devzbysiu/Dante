package dev.zbysiu.homer.suggestions

data class Suggestions(val suggestions: List<Suggestion>) {

    fun isNotEmpty(): Boolean {
        return suggestions.isNotEmpty()
    }
}
