package dev.zbysiu.homer.ui.adapter.stats.viewholder

import androidx.annotation.StringRes
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ItemStatsPagesOverTimeBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.ui.adapter.stats.model.ReadingGoalType
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPageRecordDataPoint
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPagesDiagramAction
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPagesDiagramOptions
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPagesDiagramView
import dev.zbysiu.homer.ui.custom.bookspages.MarkerViewLabelFactory
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsPagesOverTimeViewHolder(
    private val vb: ItemStatsPagesOverTimeBinding,
    private val onChangeGoalActionListener: (ReadingGoalType) -> Unit
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.BooksAndPagesOverTime) {
            when (this) {
                is BookStatsViewItem.BooksAndPagesOverTime.Empty -> {
                    showEmptyState(headerRes)
                }
                is BookStatsViewItem.BooksAndPagesOverTime.Present.Pages -> {
                    showPagesPerMonth(pagesPerMonths, readingGoal.pagesPerMonth)
                }
                is BookStatsViewItem.BooksAndPagesOverTime.Present.Books -> {
                    showBooksPerMonth(booksPerMonths, readingGoal.booksPerMonth)
                }
            }
        }
    }

    private fun showEmptyState(@StringRes headerRes: Int) {
        vb.itemBooksPagesOverTimeHeader.setHeaderTitleResource(headerRes)
        vb.itemPagesOverTimeEmpty.root.setVisible(true)
        vb.itemStatsPagesOverTimeContent.setVisible(false)
    }

    private fun showPagesPerMonth(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        pagesPerMonthGoal: Int?
    ) {
        vb.itemBooksPagesOverTimeHeader.setHeaderTitleResource(R.string.statistics_header_pages_over_time)
        vb.itemPagesOverTimeEmpty.root.setVisible(false)
        vb.itemStatsPagesOverTimeContent.setVisible(true)

        vb.itemPagesStatsDiagramView.apply {

            headerTitle = if (pagesPerMonthGoal != null) {
                context.getString(R.string.set_pages_goal_header_with_goal, pagesPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = BooksAndPagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick {
                onChangeGoalActionListener(ReadingGoalType.PAGES)
            }
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.pages_formatted)
            )
            readingGoal(pagesPerMonthGoal, BooksAndPagesDiagramView.LimitLineOffsetType.PAGES)
        }
    }

    private fun showBooksPerMonth(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        booksPerMonthGoal: Int?
    ) {
        vb.itemBooksPagesOverTimeHeader.setHeaderTitleResource(R.string.statistics_header_books_over_time)
        vb.itemPagesOverTimeEmpty.root.setVisible(false)
        vb.itemStatsPagesOverTimeContent.setVisible(true)

        vb.itemPagesStatsDiagramView.apply {

            headerTitle = if (booksPerMonthGoal != null) {
                context.getString(R.string.set_books_goal_header_with_goal, booksPerMonthGoal)
            } else context.getString(R.string.set_goal_header_no_goal)

            action = BooksAndPagesDiagramAction.Action(context.getString(R.string.set_goal))
            registerOnActionClick {
                onChangeGoalActionListener(ReadingGoalType.BOOKS)
            }
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.books_formatted)
            )
            readingGoal(booksPerMonthGoal, BooksAndPagesDiagramView.LimitLineOffsetType.BOOKS)
        }
    }
}
