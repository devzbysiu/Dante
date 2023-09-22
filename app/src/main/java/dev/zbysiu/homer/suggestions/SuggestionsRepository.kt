package dev.zbysiu.homer.suggestions

import dev.zbysiu.homer.core.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope

interface SuggestionsRepository {

    fun loadSuggestions(
        accessTimestamp: Long = System.currentTimeMillis(),
        scope: CoroutineScope
    ): Single<Suggestions>

    fun reportSuggestion(suggestionId: String, scope: CoroutineScope): Completable

    fun getUserReportedSuggestions(): Single<List<String>>

    fun suggestBook(bookEntity: BookEntity, recommendation: String): Completable
}