package dev.zbysiu.homer.stats

import dev.zbysiu.homer.core.book.BareBoneBook

data class MostActiveMonth(
    val monthAsString: String,
    val books: List<BareBoneBook>
) {

    val finishedBooks: Int
        get() = books.size
}
