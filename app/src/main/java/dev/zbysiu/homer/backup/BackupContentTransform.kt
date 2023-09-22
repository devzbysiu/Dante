package dev.zbysiu.homer.backup

import android.os.Build
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupData
import dev.zbysiu.homer.backup.model.BackupItem
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.util.singleOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

class BackupContentTransform(
    private val backupStorageProvider: BackupStorageProvider,
    private val fileNameSupplier: (timestamp: Long, books: Int) -> String
) {

    private val gson: Gson = Gson()

    fun createActualBackupData(backupContent: BackupContent): Single<BackupData> {
        return singleOf {
            val timestamp = System.currentTimeMillis()
            val fileName = fileNameSupplier(timestamp, backupContent.books.size)
            val metadata = bundleMetadataForStorage(backupContent.books.size, fileName, timestamp)

            val item = BackupItem(metadata, backupContent.books, backupContent.records)
            val content = gson.toJson(item)

            BackupData(fileName, content)
        }
    }

    private fun bundleMetadataForStorage(
        books: Int,
        fileName: String,
        timestamp: Long
    ): BackupMetadata.Standard {
        return BackupMetadata.Standard(
            id = fileName,
            fileName = fileName,
            timestamp = timestamp,
            books = books,
            storageProvider = backupStorageProvider,
            device = Build.MODEL
        )
    }

    fun createBackupContentFromBackupData(content: String): Single<BackupContent> {
        return singleOf {
            gson.fromJson(content, object : TypeToken<BackupContent>() {}.type)
        }
    }
}