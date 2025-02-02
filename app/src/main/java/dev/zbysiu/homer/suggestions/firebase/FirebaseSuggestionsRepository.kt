package dev.zbysiu.homer.suggestions.firebase

import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.login.LoginRepository
import dev.zbysiu.homer.suggestions.BookSuggestionEntity
import dev.zbysiu.homer.suggestions.SuggestionRequest
import dev.zbysiu.homer.suggestions.Suggestions
import dev.zbysiu.homer.suggestions.SuggestionsRepository
import dev.zbysiu.homer.suggestions.cache.SuggestionsCache
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.tracking.Tracker
import dev.zbysiu.tracking.event.DanteTrackingEvent
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Days
import kotlin.math.absoluteValue

class FirebaseSuggestionsRepository(
    private val firebaseSuggestionsApi: FirebaseSuggestionsApi,
    private val schedulers: SchedulerFacade,
    private val loginRepository: LoginRepository,
    private val suggestionsCache: SuggestionsCache,
    private val tracker: Tracker
) : SuggestionsRepository {

    override fun loadSuggestions(accessTimestamp: Long, scope: CoroutineScope): Single<Suggestions> {
        return shouldUseRemoteData(accessTimestamp)
            .flatMap { useRemoteSuggestions ->
                if (useRemoteSuggestions) {
                    loadRemoteSuggestions(scope)
                } else {
                    loadCachedSuggestions()
                }
            }
    }

    private fun shouldUseRemoteData(accessTimestamp: Long): Single<Boolean> {
        return suggestionsCache.lastCacheTimestamp()
            .map { lastCacheTimestamp ->

                if (lastCacheTimestamp == -1L) {
                    true // True if not set yet
                } else {
                    isLastCacheTimestampExpired(lastCacheTimestamp, accessTimestamp)
                }
            }
    }

    private fun isLastCacheTimestampExpired(
        lastCacheTimestamp: Long,
        accessTimestamp: Long
    ): Boolean {

        val cacheTime = DateTime(lastCacheTimestamp)
        val currentTime = DateTime(accessTimestamp)

        val daysBetween = Days.daysBetween(cacheTime, currentTime).days.absoluteValue

        return daysBetween >= DAYS_UPDATE_INTERVAL
    }

    private fun loadRemoteSuggestions(scope: CoroutineScope): Single<Suggestions> {
        return loginRepository.getAuthorizationHeader()
            .flatMap(firebaseSuggestionsApi::getSuggestions)
            .doOnSuccess { suggestions ->
                if (suggestions.isNotEmpty()) {
                    cacheRemoteSuggestions(suggestions, scope)
                }
            }
            .subscribeOn(schedulers.io)
    }

    private fun cacheRemoteSuggestions(suggestions: Suggestions, scope: CoroutineScope) {
        scope.launch {
            suggestionsCache.cache(suggestions)
        }
    }

    private fun loadCachedSuggestions(): Single<Suggestions> {
        return suggestionsCache.loadSuggestions()
            .subscribeOn(schedulers.io)
    }

    override fun reportSuggestion(suggestionId: String, scope: CoroutineScope): Completable {
        return loginRepository.getAuthorizationHeader()
            .flatMapCompletable { bearerToken ->
                firebaseSuggestionsApi.reportSuggestion(bearerToken, suggestionId)
            }
            .doOnComplete {
                cacheReportedSuggestion(suggestionId, scope)
            }
            .subscribeOn(schedulers.io)
    }

    override fun getUserReportedSuggestions(): Single<List<String>> {
        return suggestionsCache.loadReportedSuggestions()
    }

    private fun cacheReportedSuggestion(suggestionId: String, scope: CoroutineScope) {
        scope.launch {
            suggestionsCache.cacheSuggestionReport(suggestionId)
        }
    }

    override fun suggestBook(bookEntity: BookEntity, recommendation: String): Completable {
        return loginRepository.getAuthorizationHeader()
            .flatMapCompletable { bearerToken ->
                firebaseSuggestionsApi.suggestBook(
                    bearerToken,
                    SuggestionRequest(
                        BookSuggestionEntity.ofBookEntity(bookEntity),
                        recommendation
                    )
                )
            }
            .doOnComplete {
                tracker.track(DanteTrackingEvent.SuggestBook(bookEntity.title))
            }
            .subscribeOn(schedulers.io)
    }

    companion object {

        private const val DAYS_UPDATE_INTERVAL = 3
    }
}