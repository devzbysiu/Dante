package dev.zbysiu.homer.stats

import dev.zbysiu.homer.core.book.BareBoneBook

data class FavoriteAuthor(
    val author: String,
    val books: List<BareBoneBook>
) {

    val bookUrls: List<String?>
        get() = books.map { it.thumbnailAddress }
}
