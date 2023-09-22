package dev.zbysiu.homer.suggestions.cache

import dev.zbysiu.homer.suggestions.Suggestions
import io.reactivex.Single

interface SuggestionsCache {

    fun lastCacheTimestamp(): Single<Long>

    suspend fun cache(suggestions: Suggestions)

    fun loadSuggestions(): Single<Suggestions>

    suspend fun cacheSuggestionReport(suggestionId: String)

    fun loadReportedSuggestions(): Single<List<String>>
}