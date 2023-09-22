package dev.zbysiu.homer.backup

import androidx.fragment.app.FragmentActivity
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.util.RestoreStrategy
import dev.zbysiu.homer.backup.provider.BackupProvider
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.data.PageRecordDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    06.05.2019
 */
interface BackupRepository {

    val backupProvider: List<BackupProvider>

    fun setLastBackupTime(timeInMillis: Long)

    fun observeLastBackupTime(): Observable<Long>

    fun getBackups(): Single<List<BackupMetadataState>>

    fun initialize(activity: FragmentActivity, forceReload: Boolean): Completable

    fun close(): Completable

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(
        backupContent: BackupContent,
        backupStorageProvider: BackupStorageProvider
    ): Completable

    fun restoreBackup(
        entry: BackupMetadata,
        bookRepository: BookRepository,
        pageRecordDao: PageRecordDao,
        strategy: RestoreStrategy
    ): Completable
}