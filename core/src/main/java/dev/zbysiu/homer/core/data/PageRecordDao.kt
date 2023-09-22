package dev.zbysiu.homer.core.data

import dev.zbysiu.homer.core.book.BookId
import dev.zbysiu.homer.core.book.PageRecord
import dev.zbysiu.homer.util.RestoreStrategy
import io.reactivex.Completable
import io.reactivex.Observable

interface PageRecordDao {

    fun insertPageRecordForBookId(
        bookId: BookId,
        fromPage: Int,
        toPage: Int,
        nowInMillis: Long
    ): Completable

    fun updatePageRecord(pageRecord: PageRecord, fromPage: Int?, toPage: Int?): Completable

    fun deletePageRecordForBook(pageRecord: PageRecord): Completable

    fun deleteAllPageRecordsForBookId(bookId: BookId): Completable

    fun pageRecordsForBook(bookId: BookId): Observable<List<PageRecord>>

    fun allPageRecords(): Observable<List<PageRecord>>

    fun restoreBackup(pageRecords: List<PageRecord>, strategy: RestoreStrategy): Completable
}