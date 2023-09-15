package at.shockbytes.dante.core.network

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.network.google.BookDetailsApi
import io.reactivex.Observable

class DefaultDetailsDownloader(
    private val api: BookDetailsApi,
) : DetailsDownloader {

    override fun downloadDetails(url: String): Observable<BookEntity> {
        return Observable.just(BookEntity(title = "Downloaded title"))
//        return api.downloadBookDetails(url)
    }
}
