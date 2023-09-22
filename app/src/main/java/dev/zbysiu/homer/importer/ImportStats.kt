package dev.zbysiu.homer.importer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ImportStats {

    @Parcelize
    data class Success(
        val importedBooks: Int,
        val readLaterBooks: Int,
        val currentlyReadingBooks: Int,
        val readBooks: Int
    ) : ImportStats(), Parcelable

    object NoBooks : ImportStats()
}
