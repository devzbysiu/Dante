package dev.zbysiu.homer.ui.adapter.timeline.viewholder

import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ItemTimeLineHeaderBinding
import dev.zbysiu.homer.timeline.TimeLineItem
import at.shockbytes.util.adapter.BaseAdapter

class MonthHeaderViewHolder(
    private val vb: ItemTimeLineHeaderBinding
) : BaseAdapter.ViewHolder<TimeLineItem>(vb.root) {

    override fun bindToView(content: TimeLineItem, position: Int) {
        with(content as TimeLineItem.MonthHeader) {
            val monthStr = vb.root.context.resources.getStringArray(R.array.months)[month - 1]
            vb.tvTimeLineHeader.text = vb.root.context.getString(R.string.date_month_and_year, monthStr, year.toString())
        }
    }
}