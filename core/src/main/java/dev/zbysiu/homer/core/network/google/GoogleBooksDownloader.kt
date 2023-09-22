package dev.zbysiu.homer.core.network.google

import dev.zbysiu.homer.core.book.BookSuggestion
import dev.zbysiu.homer.core.network.BookDownloader
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import io.reactivex.Observable

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class GoogleBooksDownloader(
    private val api: GoogleBooksApi,
    private val schedulerFacade: SchedulerFacade
) : BookDownloader {

    override fun downloadBook(isbn: String): Observable<BookSuggestion> {
        return api
            .downloadBookSuggestion(isbn)
            .observeOn(schedulerFacade.ui)
            .subscribeOn(schedulerFacade.io)
    }
}
