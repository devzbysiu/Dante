package dev.zbysiu.homer.stats

import dev.zbysiu.homer.core.book.BareBoneBook

data class BookWithDuration(
    val book: BareBoneBook,
    val days: Int
)
