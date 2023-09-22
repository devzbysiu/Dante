package dev.zbysiu.homer.ui.adapter.timeline

import android.content.Context
import android.view.LayoutInflater
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.timeline.TimeLineItem
import at.shockbytes.util.adapter.MultiViewHolderBaseAdapter

class TimeLineAdapter(
    context: Context,
    imageLoader: ImageLoader,
    onItemClickListener: OnItemClickListener<TimeLineItem>
) : MultiViewHolderBaseAdapter<TimeLineItem>(context, onItemClickListener) {

    override val vhFactory = TimeLineViewHolderFactory(LayoutInflater.from(context), imageLoader)
}