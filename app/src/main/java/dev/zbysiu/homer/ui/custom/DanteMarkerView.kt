package dev.zbysiu.homer.ui.custom

import android.content.Context
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.DanteMarkerViewBinding
import dev.zbysiu.homer.ui.custom.bookspages.MarkerViewLabelFactory
import dev.zbysiu.homer.util.layoutInflater
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight

class DanteMarkerView @JvmOverloads constructor(
    context: Context,
    chartView: Chart<*>,
    private val labelFactory: MarkerViewLabelFactory
) : MarkerView(context, R.layout.dante_marker_view) {

    private val vb = DanteMarkerViewBinding.inflate(context.layoutInflater(), this, true)

    init {
        setChartView(chartView)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        e?.let { entry ->
            when (entry) {
                is PieEntry -> handlePieEntry(entry)
                else -> handleGenericEntry(entry)
            }
        }

        super.refreshContent(e, highlight)
    }

    private fun handlePieEntry(entry: PieEntry) {
        vb.tvDanteMarkerView.text = labelFactory.createLabelForValue(context, entry.value)
    }

    private fun handleGenericEntry(entry: Entry) {
        val idx = entry.x.toInt().dec()
        if (idx >= 0) {
            vb.tvDanteMarkerView.text = labelFactory.createLabelForIndex(context, idx)
        }
    }
}