package dev.zbysiu.homer.ui.adapter.stats.viewholder

import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ItemStatsBooksAndPagesBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.stats.BooksPagesInfo
import dev.zbysiu.homer.ui.custom.rbc.RelativeBarChartData
import dev.zbysiu.homer.ui.custom.rbc.RelativeBarChartEntry
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsBookAndPagesViewHolder(
    private val vb: ItemStatsBooksAndPagesBinding
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.BooksAndPages) {
            when (this) {
                BookStatsViewItem.BooksAndPages.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.BooksAndPages.Present -> {
                    showBooksAndPages(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsBooksAndPagesEmpty.root.setVisible(true)
        vb.itemStatsBooksAndPagesContent.setVisible(false)
    }

    private fun showBooksAndPages(content: BookStatsViewItem.BooksAndPages.Present) {
        vb.itemStatsBooksAndPagesEmpty.root.setVisible(false)
        vb.itemStatsBooksAndPagesContent.setVisible(true)

        with(content) {
            setBooks(booksAndPages.books)
            setPages(booksAndPages.pages)
        }
    }

    private fun setBooks(books: BooksPagesInfo.Books) {

        val entries = listOf(
            RelativeBarChartEntry(
                books.waiting.toFloat(),
                R.color.tabcolor_upcoming
            ),
            RelativeBarChartEntry(
                books.reading.toFloat(),
                R.color.tabcolor_current
            ),
            RelativeBarChartEntry(
                books.read.toFloat(),
                R.color.tabcolor_done
            )
        )

        vb.rbcItemStatsBooks.post {
            vb.rbcItemStatsBooks.setChartData(RelativeBarChartData(entries))
        }

        vb.tvItemStatsBooksWaiting.text = vb.root.context.getString(R.string.books_waiting, books.waiting)
        vb.tvItemStatsBooksReading.text = vb.root.context.getString(R.string.books_reading, books.reading)
        vb.tvItemStatsBooksRead.text = vb.root.context.getString(R.string.books_read, books.read)
    }

    private fun setPages(pages: BooksPagesInfo.Pages) {

        val entries = listOf(
            RelativeBarChartEntry(
                pages.waiting.toFloat(),
                R.color.tabcolor_upcoming
            ),
            RelativeBarChartEntry(
                pages.read.toFloat(),
                R.color.tabcolor_done
            )
        )

        vb.rbcItemStatsPages.post {
            vb.rbcItemStatsPages.setChartData(RelativeBarChartData(entries))
        }

        vb.tvItemStatsPagesWaiting.text = vb.root.context.getString(R.string.pages_waiting, pages.waiting)
        vb.tvItemStatsPagesRead.text = vb.root.context.getString(R.string.pages_read, pages.read)
    }

}