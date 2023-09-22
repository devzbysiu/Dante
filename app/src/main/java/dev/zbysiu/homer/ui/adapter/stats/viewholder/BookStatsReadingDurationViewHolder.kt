package dev.zbysiu.homer.ui.adapter.stats.viewholder

import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.ItemStatsReadingDurationBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsReadingDurationViewHolder(
    private val vb: ItemStatsReadingDurationBinding,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {


    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.ReadingDuration) {

            when (this) {
                BookStatsViewItem.ReadingDuration.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.ReadingDuration.Present -> {
                    showReadingDuration(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsReadingDurationEmpty.root.setVisible(true)
        vb.itemStatsReadingDurationContent.setVisible(false)
    }

    private fun showReadingDuration(content: BookStatsViewItem.ReadingDuration.Present) {
        vb.itemStatsReadingDurationEmpty.root.setVisible(false)
        vb.itemStatsReadingDurationContent.setVisible(true)

        with(content) {
            vb.bareBoneBookViewSlowestBook.apply {
                setTitle(slowest.book.title)

                val slowestUrl = slowest.book.thumbnailAddress
                if (slowestUrl != null) {
                    imageLoader.loadImageWithCornerRadius(
                        vb.root.context,
                        slowestUrl,
                        imageView,
                        cornerDimension = vb.root.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
            val slowestDays = vb.root.context.resources.getQuantityString(R.plurals.days, slowest.days, slowest.days)
            vb.tvItemStatsReadingDurationSlowestDuration.text = slowestDays

            vb.bareBoneBookViewFastestBook.apply {
                setTitle(fastest.book.title)

                val fastestUrl = fastest.book.thumbnailAddress
                if (fastestUrl != null) {
                    imageLoader.loadImageWithCornerRadius(
                        vb.root.context,
                        fastestUrl,
                        imageView,
                        cornerDimension = vb.root.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
            val fastestDays = vb.root.context.resources.getQuantityString(R.plurals.days, fastest.days, fastest.days)
            vb.tvItemStatsReadingDurationFastestDuration.text = fastestDays
        }
    }
}