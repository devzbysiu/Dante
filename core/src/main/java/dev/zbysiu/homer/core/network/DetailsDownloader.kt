package dev.zbysiu.homer.core.network

import dev.zbysiu.homer.core.book.BookEntity
import io.reactivex.Observable

interface DetailsDownloader {

    fun downloadDetails(url: String): Observable<BookEntity>

}
