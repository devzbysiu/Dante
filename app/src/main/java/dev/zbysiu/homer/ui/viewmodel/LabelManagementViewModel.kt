package dev.zbysiu.homer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.util.ExceptionHandlers
import dev.zbysiu.homer.util.addTo
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class LabelManagementViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : BaseViewModel() {

    sealed class LabelState {

        object Empty : LabelState()

        data class Present(val labels: List<BookLabel>) : LabelState()
    }

    private val bookLabelState = MutableLiveData<LabelState>()
    fun getBookLabelState(): LiveData<LabelState> = bookLabelState

    private val newLabelRequestSubject = PublishSubject.create<List<BookLabel>>()
    val onCreateNewLabelRequest: Observable<List<BookLabel>> = newLabelRequestSubject

    fun requestAvailableLabels(alreadyAttachedLabels: List<BookLabel>) {
        bookRepository.bookLabelObservable
            .map { labels ->
                val filtered = labels.filter { label ->
                    alreadyAttachedLabels.none { it.labelHexColor == label.labelHexColor && it.title == label.title }
                }

                if (filtered.isNotEmpty()) {
                    LabelState.Present(filtered)
                } else {
                    LabelState.Empty
                }
            }
            .subscribe(bookLabelState::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun createNewBookLabel(newLabel: BookLabel) {
        bookRepository.createBookLabel(newLabel)
            .subscribe({
                Timber.d("Successfully created book label ${newLabel.title}.")
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    fun deleteBookLabel(bookLabel: BookLabel) {
        bookRepository.deleteBookLabel(bookLabel)
            .subscribe({
                Timber.d("Successfully deleted book label ${bookLabel.title}.")
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    fun requestCreateNewLabel() {
        bookLabelState.value?.let { state ->

            val labels = when (state) {
                LabelState.Empty -> listOf()
                is LabelState.Present -> state.labels
            }
            newLabelRequestSubject.onNext(labels)
        }
    }
}
