package dev.zbysiu.homer.core.book

/**
 * Author: Martin Macheiner
 * Date: 11.09.2017
 */
data class BookSuggestion(
    val mainSuggestion: BookEntity?,
    val otherSuggestions: List<BookEntity>
) {

    val hasSuggestions: Boolean
        get() = mainSuggestion != null
}
