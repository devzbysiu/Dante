package dev.zbysiu.homer.ui.adapter

import dev.zbysiu.homer.core.book.BookEntity

interface OnBookActionClickedListener {

    fun onDelete(book: BookEntity, onDeletionConfirmed: (Boolean) -> Unit)

    fun onShare(book: BookEntity)

    fun onEdit(book: BookEntity)

    fun onSuggest(book: BookEntity)

    fun onMoveToUpcoming(book: BookEntity)

    fun onMoveToCurrent(book: BookEntity)

    fun onMoveToDone(book: BookEntity)
}