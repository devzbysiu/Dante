package dev.zbysiu.homer.core.data.local

import dev.zbysiu.homer.core.book.BookId
import dev.zbysiu.homer.core.book.PageRecord
import dev.zbysiu.homer.core.book.realm.RealmInstanceProvider
import dev.zbysiu.homer.core.book.realm.RealmPageRecord
import dev.zbysiu.homer.core.data.PageRecordDao
import dev.zbysiu.homer.util.RestoreStrategy
import dev.zbysiu.homer.util.completableOf
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.Sort

class RealmPageRecordDao(private val realm: RealmInstanceProvider) : PageRecordDao {

    private val mapper = RealmPageRecordMapper()

    private val pageRecordClass = RealmPageRecord::class.java

    override fun insertPageRecordForBookId(
        bookId: BookId,
        fromPage: Int,
        toPage: Int,
        nowInMillis: Long
    ): Completable {
        return completableOf {
            insert(
                PageRecord(
                    bookId = bookId,
                    fromPage = fromPage,
                    toPage = toPage,
                    timestamp = nowInMillis
                )
            )
        }
    }

    override fun updatePageRecord(
        pageRecord: PageRecord,
        fromPage: Int?,
        toPage: Int?
    ): Completable {
        return Completable.fromAction {
            realm.executeTransaction { realm ->
                realm.findPageRecord(pageRecord)?.let { realmRecord ->

                    fromPage?.let { realmRecord.fromPage = fromPage }
                    toPage?.let { realmRecord.toPage = toPage }

                    realm.copyToRealmOrUpdate(realmRecord)
                }
            }
        }
    }

    override fun deletePageRecordForBook(pageRecord: PageRecord): Completable {
        return Completable.fromAction {
            realm.executeTransaction { realm ->
                realm.findPageRecord(pageRecord)?.deleteFromRealm()
            }
        }
    }

    private fun Realm.findPageRecord(pageRecord: PageRecord): RealmPageRecord? {
        return where(pageRecordClass)
            .equalTo("bookId", pageRecord.bookId)
            .and()
            .equalTo("timestamp", pageRecord.timestamp)
            .findFirst()
    }

    override fun deleteAllPageRecordsForBookId(bookId: BookId): Completable {
        return Completable.fromAction {
            realm.executeTransaction { realm ->
                realm.where(pageRecordClass)
                    .equalTo("bookId", bookId)
                    .findAll()
                    ?.deleteAllFromRealm()
            }
        }
    }

    private fun insert(pageRecord: PageRecord) {
        realm.executeTransaction { realm ->
            // Only insert if a page record does not exist yet
            if (realm.findPageRecord(pageRecord) == null) {
                realm.copyToRealm(mapper.mapFrom(pageRecord))
            }
        }
    }

    override fun pageRecordsForBook(bookId: BookId): Observable<List<PageRecord>> {
        return realm.read<RealmPageRecord>()
            .equalTo("bookId", bookId)
            .sort("timestamp", Sort.ASCENDING)
            .findAllAsync()
            .asFlowable()
            .map(mapper::mapTo)
            .toObservable()
    }

    override fun allPageRecords(): Observable<List<PageRecord>> {
        return realm.read<RealmPageRecord>()
            .sort("timestamp", Sort.ASCENDING)
            .findAllAsync()
            .asFlowable()
            .map(mapper::mapTo)
            .toObservable()
    }

    override fun restoreBackup(pageRecords: List<PageRecord>, strategy: RestoreStrategy): Completable {

        val preRestorationAction = when (strategy) {
            RestoreStrategy.MERGE -> Completable.complete() // No previous action
            RestoreStrategy.OVERWRITE -> deleteAllPageRecords()
        }

        val restorationAction = completableOf {
            pageRecords.forEach(::insert)
        }

        return preRestorationAction
            .andThen(restorationAction)
    }

    private fun deleteAllPageRecords(): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.where(pageRecordClass)
                    .findAll()
                    ?.deleteAllFromRealm()
            }
        }
    }
}