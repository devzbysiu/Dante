package dev.zbysiu.homer.core.data.local

import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookId
import dev.zbysiu.homer.core.book.BookIds
import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.core.book.realm.RealmBook
import dev.zbysiu.homer.core.book.realm.RealmBookConfig
import dev.zbysiu.homer.core.book.realm.RealmBookLabel
import dev.zbysiu.homer.core.book.realm.RealmInstanceProvider
import dev.zbysiu.homer.core.data.BookEntityDao
import dev.zbysiu.homer.util.RestoreStrategy
import dev.zbysiu.homer.util.completableOf
import dev.zbysiu.homer.util.merge
import dev.zbysiu.homer.util.singleOf
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Case
import io.realm.Sort

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class RealmBookEntityDao(private val realm: RealmInstanceProvider) : BookEntityDao {

    private val bookClass = RealmBook::class.java
    private val labelClass = RealmBookLabel::class.java

    private val labelMapper = RealmBookLabelMapper()
    private val mapper: RealmBookEntityMapper = RealmBookEntityMapper(labelMapper)

    /**
     * This must always be called inside a transaction
     */
    private val lastId: Long
        get() {
            val config = realm.read<RealmBookConfig>(refreshInstance = false).findFirst()
                ?: realm.createObject(refreshInstance = false)
            return config.getLastPrimaryKey()
        }

    override val bookObservable: Observable<List<BookEntity>>
        get() = realm.read<RealmBook>()
            .sort("id", Sort.DESCENDING)
            .findAllAsync()
            .asFlowable()
            .map { mapper.mapTo(it) }
            .toObservable()

    override val allBooks: List<BookEntity>
        get() = realm.read<RealmBook>()
            .findAll()
            .toList()
            .map { mapper.mapTo(it) }

    override val bookLabelObservable: Observable<List<BookLabel>>
        get() = realm.read<RealmBookLabel>()
            .equalTo("bookId", BookIds.default())
            .sort("title", Sort.DESCENDING)
            .distinct("title")
            .findAllAsync()
            .asFlowable()
            .map { labelMapper.mapTo(it) }
            .toObservable()

    override val booksCurrentlyReading: List<BookEntity>
        get() = realm.read<RealmBook>()
            .equalTo("ordinalState", RealmBook.State.READING.ordinal)
            .sort("id", Sort.DESCENDING)
            .findAll()
            .map { mapper.mapTo(it) }

    override operator fun get(id: BookId): Single<BookEntity> {
        return singleOf {
            realm.read<RealmBook>().equalTo("id", id).findFirst()
        }.map(mapper::mapTo)
    }

    override fun create(entity: BookEntity): Completable {
        return completableOf {
            realm.executeTransaction(refreshInstance = false) { realm ->
                val id = lastId
                entity.id = id
                realm.copyToRealm(mapper.mapFrom(entity))
            }
        }
    }

    override fun update(entity: BookEntity): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.copyToRealmOrUpdate(mapper.mapFrom(entity))
            }
        }
    }

    override fun updateCurrentPage(bookId: BookId, currentPage: Int): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.where(bookClass)
                    .equalTo("id", bookId)
                    .findFirst()
                    ?.let { realmBook ->
                        realmBook.currentPage = currentPage
                        realm.copyToRealmOrUpdate(realmBook)
                    }
            }
        }
    }

    override fun delete(id: BookId): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.where(bookClass)
                    .equalTo("id", id)
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

    override fun search(query: String): Observable<List<BookEntity>> {
        return realm.read<RealmBook>()
            .contains("title", query, Case.INSENSITIVE)
            .or()
            .contains("author", query, Case.INSENSITIVE)
            .or()
            .contains("subTitle", query, Case.INSENSITIVE)
            .findAllAsync()
            .asFlowable()
            .map { mapper.mapTo(it) }
            .toObservable()
    }

    override fun restoreBackup(
        backupBooks: List<BookEntity>,
        strategy: RestoreStrategy
    ): Completable {
        return when (strategy) {
            RestoreStrategy.MERGE -> mergeBackupRestore(backupBooks)
            RestoreStrategy.OVERWRITE -> overwriteBackupRestore(backupBooks)
        }
    }

    override fun createBookLabel(bookLabel: BookLabel): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.copyToRealm(labelMapper.mapFrom(bookLabel))
            }
        }
    }

    override fun deleteBookLabel(bookLabel: BookLabel): Completable {
        return Completable.create { emitter ->
            realm.executeTransaction { realm ->
                val labels = realm.where(labelClass)
                    .equalTo("title", bookLabel.title)
                    .and()
                    .equalTo("bookId", bookLabel.bookId)
                    .findAll()

                if (labels != null && labels.isNotEmpty()) {
                    labels.forEach { rbl ->
                        rbl.deleteFromRealm()
                    }
                    emitter.onComplete()
                } else {
                    emitter.tryOnError(RealmBookLabelDeletionException(bookLabel.title))
                }
            }
        }
    }

    private fun mergeBackupRestore(backupBooks: List<BookEntity>): Completable {
        return singleOf { realm.read<RealmBook>().findAll().toList() }
            .map { books ->
                backupBooks.filter { book ->
                    books.none { it.title == book.title }
                }
            }
            .flatMapCompletable { books ->
                books.map(::create).merge()
            }
    }

    private fun overwriteBackupRestore(backupBooks: List<BookEntity>): Completable {
        val createBackupBooks = backupBooks.map(::create).merge()
        return deleteAllBooks().andThen(createBackupBooks)
    }

    private fun deleteAllBooks(): Completable {
        return completableOf {
            val stored = realm.read<RealmBook>().findAll()
            realm.executeTransaction {
                stored.deleteAllFromRealm()
            }
        }
    }
}