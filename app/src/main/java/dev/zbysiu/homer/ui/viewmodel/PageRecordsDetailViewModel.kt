package dev.zbysiu.homer.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.zbysiu.homer.core.book.BookId
import dev.zbysiu.homer.core.book.BookIds
import dev.zbysiu.homer.core.book.PageRecord
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.data.PageRecordDao
import dev.zbysiu.homer.ui.adapter.pagerecords.PageRecordDetailItem
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.indexOfOrNull
import dev.zbysiu.homer.util.isLastIndexIn
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import javax.inject.Inject

class PageRecordsDetailViewModel @Inject constructor(
    private val pageRecordDao: PageRecordDao,
    private val bookRepository: BookRepository,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    private val dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy")

    private val records = MutableLiveData<List<PageRecordDetailItem>>()
    fun getRecords(): LiveData<List<PageRecordDetailItem>> = records

    private val onBookChangedSubject = PublishSubject.create<Unit>()
    fun onBookChangedEvent(): Observable<Unit> = onBookChangedSubject

    private var bookId: BookId = BookIds.default()

    private var cachedRecords = listOf<PageRecord>()

    fun initialize(bookId: BookId) {
        this.bookId = bookId
        pageRecordDao.pageRecordsForBook(bookId)
            .doOnNext(::cachePageRecords)
            .map(::mapPageRecordToPageRecordDetailItem)
            .subscribe(records::postValue, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun cachePageRecords(cached: List<PageRecord>) {
        cachedRecords = cached
    }

    private fun mapPageRecordToPageRecordDetailItem(
        pageRecords: List<PageRecord>
    ): List<PageRecordDetailItem> {
        return pageRecords.map { record ->

            val formattedPagesRead = "${record.fromPage} - ${record.toPage}"
            val formattedDate = dateFormat.print(record.timestamp)

            PageRecordDetailItem(record, formattedPagesRead, formattedDate)
        }
    }

    fun deletePageRecord(pageRecord: PageRecord) {

        val index = cachedRecords.indexOfOrNull(pageRecord) ?: return

        val preAction: Completable = when {
            // Single entry, reset current page to 0
            index == 0 && cachedRecords.size == 1 -> {
                updateCurrentPage(0)
            }
            // Last index, just update current page to page of previous entry
            index.isLastIndexIn(cachedRecords) -> {
                val previousRecord = cachedRecords[index.dec()]
                updateCurrentPage(previousRecord.toPage)
            }
            // More than one entries and not last entry, perform normal stitching
            else -> {
                val nextRecord = cachedRecords[index.inc()]
                pageRecordDao.updatePageRecord(nextRecord, fromPage = pageRecord.fromPage, toPage = null)
            }
        }

        Completable
            .concat(
                listOf(
                    preAction,
                    pageRecordDao.deletePageRecordForBook(pageRecord) // Eventually delete page record
                )
            )
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
            .subscribe({
                initialize(bookId)
                onBookChangedSubject.onNext(Unit)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun updateCurrentPage(currentPage: Int): Completable {
        return bookRepository.updateCurrentPage(bookId, currentPage)
    }
}