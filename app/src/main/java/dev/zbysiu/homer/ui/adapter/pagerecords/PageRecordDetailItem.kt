package dev.zbysiu.homer.ui.adapter.pagerecords

import dev.zbysiu.homer.core.book.PageRecord

data class PageRecordDetailItem(
    val pageRecord: PageRecord,
    val formattedPagesRead: String,
    val formattedDate: String
)