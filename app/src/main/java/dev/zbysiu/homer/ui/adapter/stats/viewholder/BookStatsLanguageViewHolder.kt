package dev.zbysiu.homer.ui.adapter.stats.viewholder

import androidx.core.content.ContextCompat
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.Languages
import dev.zbysiu.homer.databinding.ItemStatsLanguagesBinding
import dev.zbysiu.homer.stats.BookStatsViewItem
import dev.zbysiu.homer.ui.custom.DanteMarkerView
import dev.zbysiu.homer.ui.custom.bookspages.MarkerViewLabelFactory
import dev.zbysiu.homer.util.getThemeFont
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class BookStatsLanguageViewHolder(
    private val vb: ItemStatsLanguagesBinding
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.LanguageDistribution) {
            when (this) {
                BookStatsViewItem.LanguageDistribution.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.LanguageDistribution.Present -> {
                    showLanguageChart(languages)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsLanguagesEmpty.root.setVisible(true)
        vb.chartItemStatsLanguage.setVisible(false)
    }

    private fun showLanguageChart(languages: Map<Languages, Int>) {
        vb.itemStatsLanguagesEmpty.root.setVisible(false)
        vb.chartItemStatsLanguage.setVisible(true)

        val entries = languages.map { (language, books) ->
            val title = vb.root.context.getString(language.title)
            val iconDrawable = ContextCompat.getDrawable(vb.root.context, language.image)
            PieEntry(books.toFloat(), title, iconDrawable)
        }

        val pieDataSet = PieDataSet(entries, "").apply {
            setColors(*ColorTemplate.VORDIPLOM_COLORS)
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = ""
            }
            setDrawIcons(true)
        }

        vb.chartItemStatsLanguage.apply {
            isDrawHoleEnabled = false
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            isRotationEnabled = true

            legend.apply {
                isWordWrapEnabled = true
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                formSize = 20F
                formToTextSpace = 5f
                textColor = ContextCompat.getColor(context, R.color.colorSecondaryText)
                form = Legend.LegendForm.CIRCLE
                textSize = 13f
                typeface = context.getThemeFont()
                orientation = Legend.LegendOrientation.HORIZONTAL
                isWordWrapEnabled = true
                setDrawInside(false)
            }

            setTouchEnabled(true)
            setDrawMarkers(true)
            marker = DanteMarkerView(
                context,
                chartView = vb.chartItemStatsLanguage,
                labelFactory = MarkerViewLabelFactory.forPlainEntries(R.string.books_amount)
            )

            data = PieData(pieDataSet)
            animateXY(400, 400)
            invalidate()
        }
    }
}