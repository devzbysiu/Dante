package dev.zbysiu.homer.backup.provider.shockbytes.storage

import dev.zbysiu.homer.backup.model.BackupMetadataState

interface InactiveShockbytesBackupStorage {

    fun storeInactiveItems(items: List<BackupMetadataState>)

    fun getInactiveItems(): List<BackupMetadataState>
}
