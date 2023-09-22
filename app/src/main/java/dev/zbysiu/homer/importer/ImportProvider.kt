package dev.zbysiu.homer.importer

import dev.zbysiu.homer.core.book.BookEntity
import io.reactivex.Single

interface ImportProvider {

    val importer: Importer

    fun importFromContent(content: String): Single<List<BookEntity>>
}