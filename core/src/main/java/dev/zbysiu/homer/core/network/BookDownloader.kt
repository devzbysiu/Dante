package dev.zbysiu.homer.core.network

import dev.zbysiu.homer.core.book.BookSuggestion
import io.reactivex.Observable

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
interface BookDownloader {

    fun downloadBook(isbn: String): Observable<BookSuggestion>

    companion object {
        const val MAX_FETCH_AMOUNT = 10
    }
}
