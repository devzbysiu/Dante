package dev.zbysiu.homer.ui.adapter.stats.viewholder

import androidx.core.content.ContextCompat
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.ItemStatsLabelsBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.ui.adapter.stats.model.LabelStatsItem
import dev.zbysiu.homer.util.getThemeFont
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class BookStatsLabelsViewHolder(
    private val vb: ItemStatsLabelsBinding
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.LabelStats) {
            when (this) {
                BookStatsViewItem.LabelStats.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.LabelStats.Present -> {
                    showLabelsCharts(labels)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsLabelsEmpty.root.setVisible(true)
        vb.chartItemStatsLabels.setVisible(false)
    }

    private fun showLabelsCharts(labels: List<LabelStatsItem>) {
        vb.itemStatsLabelsEmpty.root.setVisible(false)
        vb.chartItemStatsLabels.setVisible(true)

        val entries = labels.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.size.toFloat())
        }

        val barDataSet = BarDataSet(entries, "").apply {
            setColors(*labels.map { it.color }.toIntArray())
            setDrawValues(false)
            setDrawIcons(false)
        }

        vb.chartItemStatsLabels.apply {
            description.isEnabled = false
            legend.isEnabled = false

            setDrawGridBackground(false)
            setScaleEnabled(false)
            setTouchEnabled(false)

            xAxis.apply {

                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = entries.size
                setDrawAxisLine(false)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawGridBackground(false)
                typeface = context.getThemeFont()
                textColor = ContextCompat.getColor(vb.root.context, R.color.colorPrimaryText)
                valueFormatter = IndexAxisValueFormatter(labels.map { it.title })
            }

            getAxis(YAxis.AxisDependency.LEFT).apply {
                isEnabled = false
                typeface = context.getThemeFont()
                setDrawAxisLine(false)
                setDrawGridLines(false)
                setDrawZeroLine(false)
                setDrawAxisLine(false)
            }

            getAxis(YAxis.AxisDependency.RIGHT).apply {
                isEnabled = true
                setDrawAxisLine(false)
                typeface = context.getThemeFont()
                textColor = ContextCompat.getColor(vb.root.context, R.color.colorPrimaryText)
            }

            setFitBars(true)

            data = BarData(barDataSet)
            animateXY(400, 400)
            invalidate()
        }
    }
}