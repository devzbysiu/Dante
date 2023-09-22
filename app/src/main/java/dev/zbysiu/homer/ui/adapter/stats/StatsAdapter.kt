package dev.zbysiu.homer.ui.adapter.stats

import android.content.Context
import android.view.LayoutInflater
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.util.adapter.MultiViewHolderBaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class StatsAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    private val onChangeGoalActionListener: (ReadingGoalType) -> Unit
) : MultiViewHolderBaseAdapter<BookStatsViewItem>(context) {

    fun updateData(items: List<BookStatsViewItem>) {
        data.clear()
        data.addAll(items)

        notifyDataSetChanged()
    }

    override val vhFactory: ViewHolderTypeFactory<BookStatsViewItem>
        get() = StatsViewHolderFactory(LayoutInflater.from(context), imageLoader, onChangeGoalActionListener)
}