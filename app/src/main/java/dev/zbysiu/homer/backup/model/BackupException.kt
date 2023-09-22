package dev.zbysiu.homer.backup.model

/**
 * Author:  Martin Macheiner
 * Date:    01.05.2017
 */
class BackupException(s: String, val fileName: String? = null) : Throwable(s)
