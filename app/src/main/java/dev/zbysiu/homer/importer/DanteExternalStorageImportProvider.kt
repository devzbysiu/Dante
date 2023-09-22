package dev.zbysiu.homer.importer

import dev.zbysiu.homer.backup.model.BackupItem
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.util.singleOf
import com.google.gson.Gson
import io.reactivex.Single

class DanteExternalStorageImportProvider(private val gson: Gson) : ImportProvider {

    override val importer: Importer = Importer.DANTE_EXTERNAL_STORAGE

    override fun importFromContent(content: String): Single<List<BookEntity>> {
        return singleOf {
            gson.fromJson(content, BackupItem::class.java).books
        }
    }
}