package dev.zbysiu.homer.core.network

import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.network.google.BookDetailsApi
import io.reactivex.Observable

class DefaultDetailsDownloader(
    private val api: BookDetailsApi,
) : DetailsDownloader {

    override fun downloadDetails(url: String): Observable<BookEntity> {
        return api.downloadBookDetails(url)
    }
}
