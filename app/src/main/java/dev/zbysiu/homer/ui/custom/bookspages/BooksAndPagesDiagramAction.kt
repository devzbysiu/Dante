package dev.zbysiu.homer.ui.custom.bookspages

sealed class BooksAndPagesDiagramAction {
    object Gone : BooksAndPagesDiagramAction()
    object Overflow : BooksAndPagesDiagramAction()
    data class Action(val title: String) : BooksAndPagesDiagramAction()
}