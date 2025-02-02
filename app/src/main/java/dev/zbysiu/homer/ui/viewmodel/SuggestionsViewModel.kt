package dev.zbysiu.homer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.suggestions.BookSuggestionEntity
import dev.zbysiu.homer.suggestions.Suggestion
import dev.zbysiu.homer.suggestions.Suggestions
import dev.zbysiu.homer.suggestions.SuggestionsRepository
import dev.zbysiu.homer.ui.adapter.suggestions.SuggestionsAdapterItem
import dev.zbysiu.homer.util.ExceptionHandlers
import dev.zbysiu.homer.util.explanations.Explanations
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.tracking.Tracker
import dev.zbysiu.tracking.event.DanteTrackingEvent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class SuggestionsViewModel @Inject constructor(
    private val suggestionsRepository: SuggestionsRepository,
    private val bookRepository: BookRepository,
    private val tracker: Tracker,
    private val explanations: Explanations
) : BaseViewModel() {

    sealed class SuggestionsState {

        object Loading : SuggestionsState()

        data class Present(val suggestions: List<SuggestionsAdapterItem>) : SuggestionsState()

        object Error : SuggestionsState()

        object UnauthenticatedUser : SuggestionsState()

        object Empty : SuggestionsState()
    }

    sealed class SuggestionEvent {

        data class MoveToWishlistEvent(val title: String) : SuggestionEvent()

        sealed class ReportSuggestionEvent : SuggestionEvent() {

            data class Success(val title: String) : ReportSuggestionEvent()

            data class Error(val title: String) : ReportSuggestionEvent()
        }
    }

    private val onSuggestionEvent = PublishSubject.create<SuggestionEvent>()
    fun onSuggestionEvent(): Observable<SuggestionEvent> = onSuggestionEvent

    private val suggestionState = MutableLiveData<SuggestionsState>()
    fun getSuggestionState(): LiveData<SuggestionsState> = suggestionState

    fun requestSuggestions() {

        Single
            .zip(
                bookRepository.bookObservable.firstOrError(),
                suggestionsRepository.loadSuggestions(scope = viewModelScope),
                { books, suggestions -> Pair(books, suggestions) }
            )
            .doOnSubscribe {
                suggestionState.postValue(SuggestionsState.Loading)
            }
            .flatMap { (books, reports) ->
                buildSuggestionsState(books, reports)
            }
            .doOnError { throwable ->
                val errorState = if (throwable.isUnauthenticatedException()) {
                    SuggestionsState.UnauthenticatedUser
                } else {
                    SuggestionsState.Error
                }
                suggestionState.postValue(errorState)
            }
            .subscribe(suggestionState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun Throwable.isUnauthenticatedException(): Boolean {
        return (this is HttpException) && (this.code() == 403)
    }

    private fun buildSuggestionsState(
        books: List<BookEntity>,
        suggestions: Suggestions
    ): Single<SuggestionsState> {

        return suggestionsRepository.getUserReportedSuggestions()
            .map { reports ->

                val suggestedItems = suggestions.suggestions
                    .sortedBy { it.suggestionId }
                    .filter { suggestion ->
                        // Check if book isn't already added in the library
                        // and if hasn't been reported by this user
                        val bookAlreadyAdded = books.find {
                            it.title == suggestion.suggestion.title
                        } != null
                        val isReported = reports.contains(suggestion.suggestionId)
                        !bookAlreadyAdded && !isReported
                    }
                    .map(SuggestionsAdapterItem::SuggestedBook)

                when {
                    suggestedItems.isEmpty() -> SuggestionsState.Empty
                    explanations.suggestion().show -> {
                        val items = listOf(SuggestionsAdapterItem.SuggestionHint()) + suggestedItems
                        SuggestionsState.Present(items)
                    }
                    else -> SuggestionsState.Present(suggestedItems)
                }
            }
    }

    fun addSuggestionToWishlist(suggestion: Suggestion) {
        bookRepository.create(suggestion.suggestion.toBookEntity())
            .doOnComplete {
                trackAddSuggestionToWishlist(
                    suggestion.suggestionId,
                    suggestion.suggestion.title,
                    suggestion.suggester.name
                )
            }
            .subscribe({
                onSuggestionEvent.onNext(
                    SuggestionEvent.MoveToWishlistEvent(suggestion.suggestion.title)
                )
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun BookSuggestionEntity.toBookEntity(): BookEntity {
        return BookEntity(
            title = title,
            subTitle = subTitle,
            author = author,
            state = BookState.WISHLIST,
            pageCount = pageCount,
            publishedDate = publishedDate,
            isbn = isbn,
            thumbnailAddress = thumbnailAddress,
            googleBooksLink = googleBooksLink,
            language = language,
            summary = summary
        )
    }

    private fun trackAddSuggestionToWishlist(
        suggestionId: String,
        bookTitle: String,
        suggester: String
    ) {
        tracker.track(DanteTrackingEvent.AddSuggestionToWishlist(suggestionId, bookTitle, suggester))
    }

    fun dismissExplanation() {
        explanations.markSeen(explanations.suggestion())
        // Reload after mark explanation as seen
        requestSuggestions()
    }

    fun reportBookSuggestion(suggestionId: String, suggestionTitle: String) {
        suggestionsRepository.reportSuggestion(suggestionId, scope = viewModelScope)
            .doOnError(ExceptionHandlers::defaultExceptionHandler)
            .subscribe({
                onSuggestionEvent.onNext(
                    SuggestionEvent.ReportSuggestionEvent.Success(suggestionTitle)
                )
                // Reload after a book has been reported
                requestSuggestions()
            }, {
                onSuggestionEvent.onNext(
                    SuggestionEvent.ReportSuggestionEvent.Error(suggestionTitle)
                )
            })
            .addTo(compositeDisposable)
    }
}