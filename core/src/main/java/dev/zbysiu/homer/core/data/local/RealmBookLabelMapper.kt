package dev.zbysiu.homer.core.data.local

import dev.zbysiu.homer.core.book.BookLabel
import dev.zbysiu.homer.core.book.realm.RealmBookLabel
import dev.zbysiu.homer.core.data.Mapper

class RealmBookLabelMapper : Mapper<RealmBookLabel, BookLabel>() {

    override fun mapTo(data: RealmBookLabel): BookLabel {
        return BookLabel(
            title = data.title,
            hexColor = data.hexColor,
            bookId = data.bookId
        )
    }

    override fun mapFrom(data: BookLabel): RealmBookLabel {
        return RealmBookLabel(
            title = data.title,
            hexColor = data.labelHexColor.asString(),
            bookId = data.bookId
        )
    }
}