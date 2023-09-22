package dev.zbysiu.test

import dev.zbysiu.homer.core.book.BookEntity

object ObjectCreator {

    fun getPopulatedListOfBookEntities(): List<BookEntity> {
        return listOf(
            BookEntity(id = 0L, title = "Their darkest hour"),
            BookEntity(id = 1L, title = "Homo Faber"),
            BookEntity(id = 3L, title = "The ego is the enemy")
        )
    }
}