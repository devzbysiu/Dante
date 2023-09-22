package dev.zbysiu.homer.core.network.google

import dev.zbysiu.homer.core.book.BookEntity
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface BookDetailsApi {

    @GET("/")
    fun downloadBookDetails(@Query("url") url: String): Observable<BookEntity>

    companion object {
        const val SERVICE_ENDPOINT = "https://dante-backend.shuttleapp.rs"
    }
}
