package dev.zbysiu.homer.stats

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BareBoneBook
import dev.zbysiu.homer.core.book.Languages
import dev.zbysiu.homer.core.book.ReadingGoal
import dev.zbysiu.homer.ui.adapter.stats.model.LabelStatsItem
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPageRecordDataPoint

sealed class BookStatsViewItem {

    @get:LayoutRes
    abstract val layoutId: Int

    sealed class BooksAndPages : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_books_and_pages

        object Empty : BooksAndPages()

        data class Present(val booksAndPages: BooksPagesInfo) : BooksAndPages()
    }

    sealed class BooksAndPagesOverTime : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_pages_over_time

        data class Empty(@StringRes val headerRes: Int) : BooksAndPagesOverTime()

        sealed class Present : BooksAndPagesOverTime() {

            data class Pages(
                val pagesPerMonths: List<BooksAndPageRecordDataPoint>,
                val readingGoal: ReadingGoal.PagesPerMonthReadingGoal
            ) : Present()

            data class Books(
                val booksPerMonths: List<BooksAndPageRecordDataPoint>,
                val readingGoal: ReadingGoal.BooksPerMonthReadingGoal
            ) : Present()
        }
    }

    sealed class BooksPerYear : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_books_per_year

        data class Empty(@StringRes val headerRes: Int) : BooksPerYear()

        data class Present(
            val booksPerYear: List<BooksAndPageRecordDataPoint>
        ) : BooksPerYear()
    }

    sealed class ReadingDuration : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_reading_duration

        object Empty : ReadingDuration()

        data class Present(val slowest: BookWithDuration, val fastest: BookWithDuration) : ReadingDuration()
    }

    sealed class Favorites : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_favorites

        object Empty : Favorites()

        data class Present(
            val favoriteAuthor: FavoriteAuthor,
            val firstFiveStarBook: BareBoneBook?
        ) : Favorites()
    }

    sealed class LanguageDistribution : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_languages

        object Empty : LanguageDistribution()

        /**
         * @param languages Occurrences of books in a certain language mapped to the language code
         */
        data class Present(
            val languages: Map<Languages, Int>
        ) : LanguageDistribution()
    }

    sealed class LabelStats : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_labels

        object Empty : LabelStats()

        data class Present(val labels: List<LabelStatsItem>) : LabelStats()
    }

    sealed class Others : BookStatsViewItem() {

        override val layoutId: Int = R.layout.item_stats_others

        object Empty : Others()

        data class Present(
            val averageRating: Double,
            val averageBooksPerMonth: Double,
            val mostActiveMonth: MostActiveMonth?
        ) : Others()
    }
}