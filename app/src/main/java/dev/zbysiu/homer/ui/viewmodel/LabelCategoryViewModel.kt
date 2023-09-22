package dev.zbysiu.homer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.util.ExceptionHandlers
import dev.zbysiu.homer.util.addTo
import javax.inject.Inject

class LabelCategoryViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : BaseViewModel() {

    private val books = MutableLiveData<List<BookEntity>>()
    fun getBooks(): LiveData<List<BookEntity>> = books

    fun requestBooksWithLabel(label: BookLabel) {
        bookRepository.bookObservable
            .map { books ->
                books.filter { book ->
                    book.labels.any { it.title == label.title }
                }
            }
            .subscribe(books::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }
}
