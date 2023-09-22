package dev.zbysiu.homer.backup.model

import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.PageRecord

data class BackupContent(
    val books: List<BookEntity> = listOf(),
    val records: List<PageRecord> = listOf()
) {
    val isEmpty: Boolean
        get() = books.isEmpty()
}