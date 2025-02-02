package dev.zbysiu.homer.timeline

import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.util.sort.TimeLineSortStrategy
import dev.zbysiu.homer.timeline.TimeLineItem.BookTimeLineItem
import dev.zbysiu.homer.timeline.TimeLineItem.MonthHeader
import org.joda.time.DateTime

object TimeLineBuilder {

    private data class MonthAndYear(val month: Int, val year: Int)

    fun buildTimeLineItems(
        books: List<BookEntity>,
        timeLineSortStrategy: TimeLineSortStrategy
    ): List<TimeLineItem> {
        return booksForSortStrategy(books, timeLineSortStrategy)
            .map { (monthYear, books) ->
                books.mapTo(mutableListOf(MonthHeader(monthYear.month, monthYear.year))) { book ->
                    BookTimeLineItem(book.id, book.title, book.thumbnailAddress)
                }
            }
            .flatten()
    }

    private fun booksForSortStrategy(
        books: List<BookEntity>,
        timeLineSortStrategy: TimeLineSortStrategy
    ): Map<MonthAndYear, List<BookEntity>> {
        return when (timeLineSortStrategy) {
            TimeLineSortStrategy.SORT_BY_START_DATE -> {
                books
                    .filter { it.startDate > 0 && it.state != BookState.READ_LATER }
                    .sortedBy { it.startDate }
                    .asReversed()
                    .groupBy { book ->
                        DateTime(book.startDate).run {
                            MonthAndYear(monthOfYear, year)
                        }
                    }
            }
            TimeLineSortStrategy.SORT_BY_END_DATE -> {
                books
                    .filter { it.endDate > 0 }
                    .sortedBy { it.endDate }
                    .asReversed()
                    .groupBy { book ->
                        DateTime(book.endDate).run {
                            MonthAndYear(monthOfYear, year)
                        }
                    }
            }
        }
    }
}