package dev.zbysiu.homer.core.data.local

import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.core.book.realm.RealmBook
import dev.zbysiu.homer.core.data.Mapper
import io.realm.RealmList

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class RealmBookEntityMapper(
    private val labelMapper: RealmBookLabelMapper
) : Mapper<RealmBook, BookEntity>() {

    override fun mapFrom(data: BookEntity): RealmBook {
        return RealmBook(
            data.id,
            data.title,
            data.subTitle,
            data.author,
            data.pageCount,
            data.publishedDate,
            data.position,
            data.isbn,
            data.thumbnailAddress,
            data.googleBooksLink,
            data.startDate,
            data.endDate,
            data.wishlistDate,
            data.language ?: "NA",
            data.rating,
            data.currentPage,
            data.notes,
            data.summary,
            RealmList(*data.labels.map { bookLabel -> labelMapper.mapFrom(bookLabel) }.toTypedArray())
        ).apply {
            state = RealmBook.State.values()[data.state.ordinal]
        }
    }

    override fun mapTo(data: RealmBook): BookEntity {
        return BookEntity(
            data.id,
            data.title,
            data.subTitle,
            data.author,
            BookState.values()[data.state.ordinal],
            data.pageCount,
            data.publishedDate,
            data.position,
            data.isbn,
            data.thumbnailAddress,
            data.googleBooksLink,
            data.startDate,
            data.endDate,
            data.wishlistDate,
            data.language,
            data.rating,
            data.currentPage,
            data.notes,
            data.summary,
            data.labels.map { realmBookLabel -> labelMapper.mapTo(realmBookLabel) }
        )
    }
}