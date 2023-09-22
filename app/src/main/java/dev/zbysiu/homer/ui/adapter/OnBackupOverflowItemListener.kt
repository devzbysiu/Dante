package dev.zbysiu.homer.ui.adapter

import dev.zbysiu.homer.backup.model.BackupMetadata

interface OnBackupOverflowItemListener {

    fun onBackupItemDeleted(content: BackupMetadata, location: Int)

    fun onBackupItemDownloadRequest(content: BackupMetadata.WithLocalFile)

    fun onBackupItemOpenFileRequest(content: BackupMetadata.WithLocalFile)
}
