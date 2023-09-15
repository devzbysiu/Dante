package at.shockbytes.dante.core.network

import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Observable

interface DetailsDownloader {

    fun downloadDetails(url: String): Observable<BookEntity>

}
