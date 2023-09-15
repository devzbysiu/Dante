package at.shockbytes.dante.core.network.google

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookSuggestion
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface BookDetailsApi {

    @GET("volumes")
    fun downloadBookDetails(@Query("url") url: String): Observable<BookEntity>

    companion object {
        const val SERVICE_ENDPOINT = "https://google.com"
    }
}
