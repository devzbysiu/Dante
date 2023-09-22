package dev.zbysiu.homer.backup.provider.external

import android.Manifest
import androidx.fragment.app.FragmentActivity
import dev.zbysiu.homer.R
import dev.zbysiu.homer.backup.BackupContentTransform
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupItem
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadata.Companion.attachLocalFile
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.backup.model.BackupServiceConnectionException
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.backup.provider.BackupProvider
import dev.zbysiu.homer.storage.ExternalStorageInteractor
import dev.zbysiu.homer.util.permission.PermissionManager
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.homer.util.singleOf
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    28.05.2019
 */
class ExternalStorageBackupProvider(
    private val schedulers: SchedulerFacade,
    private val gson: Gson,
    private val externalStorageInteractor: ExternalStorageInteractor,
    private val permissionManager: PermissionManager
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.EXTERNAL_STORAGE
    override var isEnabled: Boolean = true

    private val contentTransform = BackupContentTransform(backupStorageProvider, ::createFileName)

    override fun initialize(activity: FragmentActivity?): Completable {
        return Completable.fromAction {

            if (activity == null) {
                isEnabled = false
                throw BackupServiceConnectionException("${this.javaClass.simpleName} requires an activity!")
            }

            checkPermissions(activity)

            // If not enabled --> do nothing, we don't have the right permissions
            if (isEnabled) {

                try {
                    externalStorageInteractor.createBaseDirectory(BASE_DIR_NAME)
                    isEnabled = true
                } catch (e: IllegalStateException) {
                    isEnabled = false
                    throw e // Rethrow exception after disabling backup provider
                }
            }
        }
    }

    override fun backup(backupContent: BackupContent): Completable {
        return contentTransform.createActualBackupData(backupContent)
            .flatMapCompletable { (fileName, content) ->
                externalStorageInteractor.writeToFileInDirectory(BASE_DIR_NAME, fileName, content)
            }
            .subscribeOn(schedulers.io)
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return externalStorageInteractor
            .listFilesInDirectory(
                BASE_DIR_NAME,
                filterPredicate = { fileName ->
                    fileName.endsWith(BACKUP_ITEM_SUFFIX)
                }
            ).map { files ->
                files.mapNotNull { backupFile ->
                    backupFileToBackupEntry(backupFile)
                }
            }
            .subscribeOn(schedulers.io)
    }

    private fun backupFileToBackupEntry(backupFile: File): BackupMetadataState? {

        return try {

            val metadata = externalStorageInteractor.readFileContent(
                BASE_DIR_NAME,
                backupFile.name
            ).let { content ->
                gson.fromJson(content, BackupItem::class.java)
                    .backupMetadata
                    // This line is necessary because the local file path is not stored
                    // within the serialized Json, it's only used when loaded
                    .attachLocalFile(backupFile, mimeType = MIME_TYPE_JSON)
            }

            // Can only be active, ExternalStorageBackupProvider does not support cached states
            BackupMetadataState.Active(metadata)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return externalStorageInteractor
            .deleteFileInDirectory(BASE_DIR_NAME, entry.fileName)
            .subscribeOn(schedulers.io)
    }

    override fun removeAllBackupEntries(): Completable {
        return externalStorageInteractor
            .deleteFilesInDirectory(BASE_DIR_NAME)
            .subscribeOn(schedulers.io)
    }

    override fun mapBackupToBackupContent(entry: BackupMetadata): Single<BackupContent> {
        return singleOf {
                externalStorageInteractor.readFileContent(BASE_DIR_NAME, entry.fileName)
            }
            .flatMap(contentTransform::createBackupContentFromBackupData)
            .subscribeOn(schedulers.io)
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    private fun checkPermissions(activity: FragmentActivity) {

        permissionManager.verifyPermissions(activity, REQUIRED_PERMISSIONS).let { hasPermissions ->

            // BackupProvider is enabled if it has permissions to read and write external storage
            isEnabled = hasPermissions

            if (!hasPermissions) {

                permissionManager.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    RC_READ_WRITE_EXT_STORAGE,
                    R.string.external_storage_rationale,
                    R.string.rationale_ask_ok,
                    R.string.rationale_ask_cancel
                )
            }
        }
    }

    private fun createFileName(timestamp: Long, books: Int): String {
        return "dante-backup-$timestamp$BACKUP_ITEM_SUFFIX"
    }

    companion object {

        private const val BASE_DIR_NAME = "Dante"
        private const val BACKUP_ITEM_SUFFIX = ".json"
        private const val MIME_TYPE_JSON = "application/json"
        private const val RC_READ_WRITE_EXT_STORAGE = 0x5321

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}