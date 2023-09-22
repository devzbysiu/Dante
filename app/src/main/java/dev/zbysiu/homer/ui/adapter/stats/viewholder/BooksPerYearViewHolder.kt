package dev.zbysiu.homer.ui.adapter.stats.viewholder

import androidx.annotation.StringRes
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ItemStatsBooksPerYearBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPageRecordDataPoint
import dev.zbysiu.homer.ui.custom.bookspages.BooksAndPagesDiagramOptions
import dev.zbysiu.homer.ui.custom.bookspages.MarkerViewLabelFactory
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BooksPerYearViewHolder(
    private val vb: ItemStatsBooksPerYearBinding
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.BooksPerYear) {
            when (this) {
                is BookStatsViewItem.BooksPerYear.Empty -> {
                    showEmptyState(headerRes)
                }
                is BookStatsViewItem.BooksPerYear.Present -> {
                    showBooksPerYear(booksPerYear)
                }
            }
        }
    }

    private fun showEmptyState(@StringRes headerRes: Int) {
        vb.itemStatsBooksPerYearHeader.setHeaderTitleResource(headerRes)
        vb.itemStatsBooksPerYearEmpty.root.setVisible(true)
        vb.itemStatsBooksPerYearContent.setVisible(false)
    }

    private fun showBooksPerYear(dataPoints: List<BooksAndPageRecordDataPoint>) {
        vb.itemStatsBooksPerYearHeader.setHeaderTitleResource(R.string.statistics_header_books_per_year)
        vb.itemStatsBooksPerYearEmpty.root.setVisible(false)
        vb.itemStatsBooksPerYearContent.setVisible(true)

        vb.itemStatsBooksPerYearDiagramView.apply {
            hideHeader()
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(isZoomable = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.books_formatted)
            )
        }
    }
}