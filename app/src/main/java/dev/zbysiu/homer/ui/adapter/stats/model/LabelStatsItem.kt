package dev.zbysiu.homer.ui.adapter.stats.model

import androidx.annotation.ColorInt

data class LabelStatsItem(
    val title: String,
    @ColorInt val color: Int,
    val size: Int
)