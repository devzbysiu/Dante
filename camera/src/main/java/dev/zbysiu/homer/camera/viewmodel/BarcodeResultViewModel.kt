package dev.zbysiu.homer.camera.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.zbysiu.homer.camera.R
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookLoadingState
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.core.book.BookSuggestion
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.homer.core.network.BookDownloader
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BarcodeResultViewModel(
    private val bookDownloader: BookDownloader,
    private val schedulerFacade: SchedulerFacade,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val bookLoadingState = MutableLiveData<BookLoadingState>()
    fun getBookLoadingState(): LiveData<BookLoadingState> = bookLoadingState

    sealed class BookStoredEvent {

        data class Success(
            val title: String,
            val state: BookState
        ) : BookStoredEvent()

        data class Error(val reason: String?) : BookStoredEvent()
    }

    private val bookStoredSubject = PublishSubject.create<BookStoredEvent>()
    fun onBookStoredEvent(): Observable<BookStoredEvent> = bookStoredSubject

    init {
        bookLoadingState.value = BookLoadingState.Loading
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun loadBook(isbn: String) {
        bookDownloader.downloadBook(isbn)
            .subscribeOn(schedulerFacade.io)
            .map { suggestion ->
                if (suggestion.hasSuggestions) {
                    BookLoadingState.Success(suggestion)
                } else {
                    BookLoadingState.Error(R.string.download_book_json_error)
                }
            }
            .subscribe({ state ->
                bookLoadingState.postValue(state)
            }, { throwable ->
                Timber.e(throwable)
                bookLoadingState.postValue(BookLoadingState.Error(R.string.download_code_error))
            })
            .addTo(compositeDisposable)
    }

    fun storeBook(bookEntity: BookEntity, state: BookState) {

        val updated = bookEntity.apply { updateState(state) }

        bookRepository.create(updated)
            .subscribe({
                bookStoredSubject.onNext(BookStoredEvent.Success(bookEntity.title, bookEntity.state))
            }, { throwable ->
                Timber.e(throwable)
                bookStoredSubject.onNext(BookStoredEvent.Error(throwable.localizedMessage))
            })
            .addTo(compositeDisposable)
    }

    fun setSelectedBook(bookSuggestion: BookSuggestion, selectedBook: BookEntity) {

        val updatedSuggestions = ArrayList(bookSuggestion.otherSuggestions).apply {
            remove(selectedBook)
            bookSuggestion.mainSuggestion?.let { mainSuggestion ->
                add(mainSuggestion)
            }
        }

        val updated = BookSuggestion(mainSuggestion = selectedBook, otherSuggestions = updatedSuggestions)
        bookLoadingState.postValue(BookLoadingState.Success(updated))
    }
}