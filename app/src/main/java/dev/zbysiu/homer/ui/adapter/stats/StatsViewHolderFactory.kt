package dev.zbysiu.homer.ui.adapter.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.ItemStatsBooksAndPagesBinding
import dev.zbysiu.homer.databinding.ItemStatsBooksPerYearBinding
import dev.zbysiu.homer.databinding.ItemStatsFavoritesBinding
import dev.zbysiu.homer.databinding.ItemStatsLabelsBinding
import dev.zbysiu.homer.databinding.ItemStatsLanguagesBinding
import dev.zbysiu.homer.databinding.ItemStatsOthersBinding
import dev.zbysiu.homer.databinding.ItemStatsPagesOverTimeBinding
import dev.zbysiu.homer.databinding.ItemStatsReadingDurationBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.ui.adapter.stats.model.ReadingGoalType
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsBookAndPagesViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsFavoritesViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsLabelsViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsLanguageViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsOthersViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsPagesOverTimeViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BookStatsReadingDurationViewHolder
import dev.zbysiu.homer.ui.adapter.stats.viewholder.BooksPerYearViewHolder
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class StatsViewHolderFactory(
    private val inflater: LayoutInflater,
    private val imageLoader: ImageLoader,
    private val onChangeGoalActionListener: (ReadingGoalType) -> Unit
) : ViewHolderTypeFactory<BookStatsViewItem> {

    override fun type(item: BookStatsViewItem): Int {
        return item.layoutId
    }

    override fun create(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<BookStatsViewItem> {
        return when (viewType) {
            R.layout.item_stats_books_and_pages -> BookStatsBookAndPagesViewHolder(ItemStatsBooksAndPagesBinding.inflate(inflater, parent, false))
            R.layout.item_stats_reading_duration -> BookStatsReadingDurationViewHolder(ItemStatsReadingDurationBinding.inflate(inflater, parent, false), imageLoader)
            R.layout.item_stats_favorites -> BookStatsFavoritesViewHolder(ItemStatsFavoritesBinding.inflate(inflater, parent, false), imageLoader)
            R.layout.item_stats_languages -> BookStatsLanguageViewHolder(ItemStatsLanguagesBinding.inflate(inflater, parent, false))
            R.layout.item_stats_others -> BookStatsOthersViewHolder(ItemStatsOthersBinding.inflate(inflater, parent, false))
            R.layout.item_stats_labels -> BookStatsLabelsViewHolder(ItemStatsLabelsBinding.inflate(inflater, parent, false))
            R.layout.item_stats_pages_over_time -> BookStatsPagesOverTimeViewHolder(ItemStatsPagesOverTimeBinding.inflate(inflater, parent, false), onChangeGoalActionListener)
            R.layout.item_stats_books_per_year -> BooksPerYearViewHolder(ItemStatsBooksPerYearBinding.inflate(inflater, parent, false))
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }
}