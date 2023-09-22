package dev.zbysiu.homer.ui.adapter

import dev.zbysiu.homer.core.book.BookLabel

interface OnLabelActionClickedListener {

    fun onLabelDeleted(label: BookLabel)

    fun onLabelColorEdit(label: BookLabel)
}
