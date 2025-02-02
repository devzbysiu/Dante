package dev.zbysiu.homer.storage

import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
interface ExternalStorageInteractor {

    @Throws(IllegalStateException::class)
    fun createBaseDirectory(directoryName: String)

    fun writeToFileInDirectory(
        directoryName: String,
        fileName: String,
        content: String
    ): Completable

    fun deleteFilesInDirectory(directoryName: String): Completable

    fun listFilesInDirectory(
        directoryName: String,
        filterPredicate: (name: String) -> Boolean
    ): Single<List<File>>

    fun readFileContent(
        directoryName: String,
        fileName: String
    ): String

    fun deleteFileInDirectory(directoryName: String, fileName: String): Completable
}