package dev.zbysiu.homer.ui.adapter.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.ItemTimeLineBookBinding
import dev.zbysiu.homer.databinding.ItemTimeLineHeaderBinding
import dev.zbysiu.homer.timeline.TimeLineItem
import dev.zbysiu.homer.ui.adapter.timeline.viewholder.BookTimeLineViewHolder
import dev.zbysiu.homer.ui.adapter.timeline.viewholder.DanteInstallViewHolder
import dev.zbysiu.homer.ui.adapter.timeline.viewholder.MonthHeaderViewHolder
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ViewHolderTypeFactory

class TimeLineViewHolderFactory(
    private val inflater: LayoutInflater,
    private val imageLoader: ImageLoader
) : ViewHolderTypeFactory<TimeLineItem> {

    override fun type(item: TimeLineItem): Int {
        return when (item) {
            is TimeLineItem.BookTimeLineItem -> R.layout.item_time_line_book
            is TimeLineItem.MonthHeader -> R.layout.item_time_line_header
            TimeLineItem.DanteInstall -> R.layout.item_time_line_install
        }
    }

    override fun create(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<TimeLineItem> {
        return when (viewType) {
            R.layout.item_time_line_book -> BookTimeLineViewHolder(ItemTimeLineBookBinding.inflate(inflater, parent, false), imageLoader)
            R.layout.item_time_line_header -> MonthHeaderViewHolder(ItemTimeLineHeaderBinding.inflate(inflater, parent, false))
            R.layout.item_time_line_install -> DanteInstallViewHolder(inflater.inflate(viewType, parent, false))
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }
}