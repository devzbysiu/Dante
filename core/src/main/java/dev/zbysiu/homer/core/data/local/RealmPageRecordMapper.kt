package dev.zbysiu.homer.core.data.local

import dev.zbysiu.homer.core.book.PageRecord
import dev.zbysiu.homer.core.book.realm.RealmPageRecord
import dev.zbysiu.homer.core.data.Mapper

class RealmPageRecordMapper : Mapper<RealmPageRecord, PageRecord>() {

    override fun mapTo(data: RealmPageRecord): PageRecord {
        return PageRecord(
                bookId = data.bookId,
                fromPage = data.fromPage,
                toPage = data.toPage,
                timestamp = data.timestamp
        )
    }

    override fun mapFrom(data: PageRecord): RealmPageRecord {

        val recordId = "${data.bookId}-${data.timestamp}"
        return RealmPageRecord(
                recordId = recordId,
                bookId = data.bookId,
                fromPage = data.fromPage,
                toPage = data.toPage,
                timestamp = data.timestamp
        )
    }
}