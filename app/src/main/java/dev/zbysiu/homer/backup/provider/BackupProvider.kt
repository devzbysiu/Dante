package dev.zbysiu.homer.backup.provider

import androidx.fragment.app.FragmentActivity
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import io.reactivex.Completable
import io.reactivex.Single

interface BackupProvider {

    val backupStorageProvider: BackupStorageProvider

    var isEnabled: Boolean

    fun initialize(activity: FragmentActivity? = null): Completable

    fun backup(backupContent: BackupContent): Completable

    fun getBackupEntries(): Single<List<BackupMetadataState>>

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun mapBackupToBackupContent(entry: BackupMetadata): Single<BackupContent>

    fun teardown(): Completable
}