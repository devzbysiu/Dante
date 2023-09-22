package dev.zbysiu.homer.ui.adapter.stats.viewholder

import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BareBoneBook
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.ItemStatsFavoritesBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.stats.FavoriteAuthor
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsFavoritesViewHolder(
    private val vb: ItemStatsFavoritesBinding,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.Favorites) {
            when (this) {
                BookStatsViewItem.Favorites.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.Favorites.Present -> {
                    showReadingDuration(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsFavoritesEmpty.root.setVisible(true)
        vb.itemStatsFavoritesContent.setVisible(false)
    }

    private fun showReadingDuration(content: BookStatsViewItem.Favorites.Present) {
        vb.itemStatsFavoritesEmpty.root.setVisible(false)
        vb.itemStatsFavoritesContent.setVisible(true)

        with(content) {
            setFavoriteAuthor(favoriteAuthor)
            setFirstFiveStarBook(firstFiveStarBook)
        }
    }

    private fun setFavoriteAuthor(favoriteAuthor: FavoriteAuthor) {
        vb.multiBareBoneBookFavoriteAuthor.apply {
            setTitle(favoriteAuthor.author)
            setMultipleBookImages(favoriteAuthor.bookUrls, imageLoader)
        }
    }

    private fun setFirstFiveStarBook(firstFiveStarBook: BareBoneBook?) {

        vb.bareBoneBookViewFirstFiveStar.setVisible(firstFiveStarBook != null)
        vb.tvItemStatsFavoritesFirstFiveStarHeader.setVisible(firstFiveStarBook != null)

        firstFiveStarBook?.let {
            vb.bareBoneBookViewFirstFiveStar.apply {
                setTitle(firstFiveStarBook.title)

                val url = firstFiveStarBook.thumbnailAddress
                if (url != null) {
                    imageLoader.loadImageWithCornerRadius(
                        vb.root.context,
                        url,
                        imageView,
                        cornerDimension = vb.root.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }
    }
}