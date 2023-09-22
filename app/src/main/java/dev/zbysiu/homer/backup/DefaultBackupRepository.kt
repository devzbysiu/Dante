package dev.zbysiu.homer.backup

import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.util.RestoreStrategy
import dev.zbysiu.homer.backup.provider.BackupProvider
import dev.zbysiu.homer.backup.model.BackupStorageProviderNotAvailableException
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookId
import dev.zbysiu.homer.core.book.PageRecord
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.data.PageRecordDao
import dev.zbysiu.homer.util.merge
import dev.zbysiu.homer.util.singleOf
import dev.zbysiu.tracking.Tracker
import dev.zbysiu.tracking.event.DanteTrackingEvent
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dev.zbysiu.homer.util.settings.delegate.edit
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DefaultBackupRepository(
    override val backupProvider: List<BackupProvider>,
    private val preferences: SharedPreferences,
    private val tracker: Tracker
) : BackupRepository {

    private val activeBackupProvider: List<BackupProvider>
        get() = backupProvider.filter { it.isEnabled }

    override fun setLastBackupTime(timeInMillis: Long) {
        preferences.edit {
            putLong(KEY_LAST_BACKUP, timeInMillis)
        }
    }

    override fun observeLastBackupTime(): Observable<Long> {
        return RxSharedPreferences.create(preferences)
            .getLong(KEY_LAST_BACKUP, 0)
            .asObservable()
    }

    override fun getBackups(): Single<List<BackupMetadataState>> {

        val activeBackupProviderSources = activeBackupProvider.map { it.getBackupEntries() }

        return Single.merge(activeBackupProviderSources)
            .collect(
                { mutableListOf() },
                { container: MutableList<BackupMetadataState>, value: List<BackupMetadataState> ->
                    container.addAll(value)
                }
            )
            .map { entries ->
                entries
                    .sortedByDescending {
                        it.timestamp
                    }
                    .toList()
            }
    }

    override fun initialize(activity: FragmentActivity, forceReload: Boolean): Completable {

        // If forceReload is set, then use the whole listBackupFiles of backup provider,
        // otherwise just use the active ones
        val provider = if (forceReload) backupProvider else activeBackupProvider

        return provider
            .map { it.initialize(activity) }
            .merge()
    }

    override fun close(): Completable {
        return Completable.concat(activeBackupProvider.map { it.teardown() })
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return getBackupProvider(entry.storageProvider).removeBackupEntry(entry)
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.concat(activeBackupProvider.map { it.removeAllBackupEntries() })
    }

    override fun backup(
        backupContent: BackupContent,
        backupStorageProvider: BackupStorageProvider
    ): Completable {
        return getBackupProvider(backupStorageProvider)
            .backup(backupContent)
            .doOnComplete {
                setLastBackupTime(System.currentTimeMillis())
                trackBackupMadeEvent(backupStorageProvider)
            }
            ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    private fun trackBackupMadeEvent(backupStorageProvider: BackupStorageProvider) {
        tracker.track(DanteTrackingEvent.BackupMadeEvent(backupStorageProvider.acronym))
    }

    override fun restoreBackup(
        entry: BackupMetadata,
        bookRepository: BookRepository,
        pageRecordDao: PageRecordDao,
        strategy: RestoreStrategy
    ): Completable {
        return getBackupProvider(entry.storageProvider)
            .mapBackupToBackupContent(entry)
            .flatMapCompletable { (books, pageRecords) ->
                val copyOfBooks = books.map { it.copy() }
                bookRepository.restoreBackup(books, strategy)
                    .andThen(restorePageRecords(bookRepository, pageRecordDao, books = copyOfBooks, pageRecords, strategy))
            }
    }

    private fun restorePageRecords(
        bookRepository: BookRepository,
        pageRecordDao: PageRecordDao,
        books: List<BookEntity>,
        pageRecords: List<PageRecord>,
        strategy: RestoreStrategy
    ): Completable {
        return singleOf {  bookRepository.allBooks }
            .map { restoredBooks ->
                val map = createIdMappingForRestoredBooks(restoredBooks, books)
                pageRecords.map { pageRecord ->
                    pageRecord.copy(bookId = map[pageRecord.bookId]
                        ?: error("Cannot find previously restored book by map lookup!"))
                }
            }
            .flatMapCompletable { mappedPageRecords ->
                pageRecordDao.restoreBackup(mappedPageRecords, strategy)
            }
    }

    private fun createIdMappingForRestoredBooks(
        restoredBooks: List<BookEntity>,
        backupBooks: List<BookEntity>
    ): Map<BookId, BookId> {
        return restoredBooks.associate { book ->

            val backupBookId = backupBooks.find {
                book.title == it.title && book.author == it.author
            }?.id ?: throw IllegalStateException("Cannot find previously restored book by title lookup!")

            backupBookId to book.id
        }
    }

    private fun getBackupProvider(source: BackupStorageProvider): BackupProvider {
        return activeBackupProvider.find { it.backupStorageProvider == source }
            ?: throw BackupStorageProviderNotAvailableException()
    }

    companion object {
        private const val KEY_LAST_BACKUP = "key_last_backup"
    }
}