package dev.zbysiu.homer.ui.custom.bookspages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import dev.zbysiu.homer.R
import dev.zbysiu.homer.databinding.PagesDiagramViewBinding
import dev.zbysiu.homer.ui.custom.DanteMarkerView
import dev.zbysiu.homer.util.getThemeFont
import dev.zbysiu.homer.util.layoutInflater
import dev.zbysiu.homer.util.setVisible
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class BooksAndPagesDiagramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val vb = PagesDiagramViewBinding.inflate(context.layoutInflater(), this, true)

    private val chart: LineChart
        get() = vb.lcPageRecords

    var headerTitle: String = ""
        set(value) {
            field = value
            vb.tvPageRecordHeader.text = value
        }

    var action: BooksAndPagesDiagramAction = BooksAndPagesDiagramAction.Gone
        set(value) {
            field = value
            setActionVisibility(value)
        }

    val actionView: View
        get() {
            return when (action) {
                is BooksAndPagesDiagramAction.Overflow -> vb.ivPageRecordOverflow
                is BooksAndPagesDiagramAction.Action -> vb.btnPageRecordAction
                is BooksAndPagesDiagramAction.Gone -> throw IllegalStateException("No action view for action type GONE")
            }
        }

    fun hideHeader() {
        vb.tvPageRecordHeader.setVisible(false)
        vb.btnPageRecordAction.setVisible(false)
        vb.ivPageRecordOverflow.setVisible(false)
    }

    fun setData(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        diagramOptions: BooksAndPagesDiagramOptions = BooksAndPagesDiagramOptions(),
        labelFactory: MarkerViewLabelFactory
    ) {

        val formattedDates = dataPoints.map { it.formattedDate }
        val dataSet = createDataSet(createDataSetEntries(dataPoints, diagramOptions.initialZero))

        styleChartAndSetData(dataSet, labelFactory, formattedDates, diagramOptions.isZoomable)
    }

    private fun createDataSetEntries(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        initialZero: Boolean
    ): List<Entry> {
        return dataPoints
            .mapIndexed { index, dp ->
                Entry(index.inc().toFloat(), dp.value.toFloat())
            }
            .toMutableList()
            .apply {
                if (initialZero) {
                    add(0, BarEntry(0f, 0f)) // Initial entry
                }
            }
    }

    private fun createDataSet(entries: List<Entry>): LineDataSet {
        return LineDataSet(entries, "").apply {
            setColor(ContextCompat.getColor(context, R.color.page_record_data), 255)
            setDrawValues(false)
            setDrawIcons(false)
            setDrawFilled(true)
            setDrawHighlightIndicators(false)
            isHighlightEnabled = true
            setCircleColor(ContextCompat.getColor(context, R.color.page_record_data))
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.page_record_gradient)
        }
    }

    private fun styleChartAndSetData(
        dataSet: LineDataSet,
        markerViewLabelFactory: MarkerViewLabelFactory,
        formattedDates: List<String>,
        isZoomable: Boolean
    ) {
        chart.apply {
            // Clear old values first, might be null (Java...)
            data?.clearValues()

            description.isEnabled = false
            legend.isEnabled = false

            setTouchEnabled(true)
            setDrawGridBackground(false)
            setScaleEnabled(isZoomable)
            isDragEnabled = isZoomable

            xAxis.apply {
                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = formattedDates.size / 2
                setDrawAxisLine(false)
                labelRotationAngle = -30f
                textSize = 8f
                setDrawGridLines(false)
                typeface = context.getThemeFont()
                setDrawAxisLine(false)
                setDrawGridBackground(false)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
                valueFormatter = IndexAxisValueFormatter(formattedDates)
            }

            getAxis(YAxis.AxisDependency.LEFT).apply {
                isEnabled = true
                setDrawLimitLinesBehindData(true)
                setDrawGridLines(false)
                setDrawZeroLine(false)
                setDrawAxisLine(false)
                typeface = context.getThemeFont()
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
            }
            getAxis(YAxis.AxisDependency.RIGHT).apply {
                isEnabled = false
                setDrawAxisLine(false)
                textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
            }

            setDrawMarkers(true)
            marker = DanteMarkerView(context, chartView = chart, labelFactory = markerViewLabelFactory)

            data = LineData(dataSet)
            invalidate()
        }
    }

    fun registerOnActionClick(clickAction: () -> Unit) {
        when (action) {
            is BooksAndPagesDiagramAction.Overflow -> vb.ivPageRecordOverflow.setOnClickListener { clickAction() }
            is BooksAndPagesDiagramAction.Action -> vb.btnPageRecordAction.setOnClickListener { clickAction() }
            is BooksAndPagesDiagramAction.Gone -> Unit // Do nothing
        }
    }

    private fun setActionVisibility(value: BooksAndPagesDiagramAction) {
        when (value) {
            BooksAndPagesDiagramAction.Overflow -> {
                vb.ivPageRecordOverflow.setVisible(true)
                vb.btnPageRecordAction.setVisible(false)
            }
            BooksAndPagesDiagramAction.Gone -> {
                vb.ivPageRecordOverflow.setVisible(false)
                vb.btnPageRecordAction.setVisible(false)
            }
            is BooksAndPagesDiagramAction.Action -> {
                vb.ivPageRecordOverflow.setVisible(false)
                vb.btnPageRecordAction.apply {
                    setVisible(true)
                    text = value.title
                }
            }
        }
    }

    fun readingGoal(value: Int?, offsetType: LimitLineOffsetType) {

        // Anyway, remove all limit lines
        chart.getAxis(YAxis.AxisDependency.LEFT).apply {
            removeAllLimitLines()
            // setDrawGridLines(false)
        }

        if (value != null) {
            createLimitLine(value.toFloat())
                .let(::addLimitLineToChart)

            checkLineBoundaries(value.toFloat(), offsetType)
        }
    }

    private fun checkLineBoundaries(value: Float, offsetType: LimitLineOffsetType) {
        val yAxis = chart.getAxis(YAxis.AxisDependency.LEFT)

        when (yAxis.isLimitLineShown(value)) {
            LimitLinePosition.EXCEEDS_UPPER_BOUND -> {
                yAxis.axisMaximum = value.plus(offsetType.offset)
            }
            LimitLinePosition.EXCEEDS_LOWER_BOUND -> {
                yAxis.axisMinimum = value.minus(offsetType.offset)
            }
            LimitLinePosition.IS_VISIBLE -> Unit
        }
    }

    private enum class LimitLinePosition {
        EXCEEDS_UPPER_BOUND,
        EXCEEDS_LOWER_BOUND,
        IS_VISIBLE
    }

    private fun YAxis.isLimitLineShown(limit: Float): LimitLinePosition {
        return when {
            limit <= axisMinimum -> LimitLinePosition.EXCEEDS_LOWER_BOUND
            limit >= axisMaximum -> LimitLinePosition.EXCEEDS_UPPER_BOUND
            else -> LimitLinePosition.IS_VISIBLE
        }
    }

    private fun createLimitLine(value: Float): LimitLine {
        return LimitLine(value, context.getString(R.string.reading_goal)).apply {
            lineColor = ContextCompat.getColor(context, R.color.tabcolor_done)
            lineWidth = 0.8f
            enableDashedLine(20f, 20f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            typeface = context.getThemeFont()
            textSize = 10f
            textColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
        }
    }

    private fun addLimitLineToChart(limitLine: LimitLine) {
        chart.getAxis(YAxis.AxisDependency.LEFT).apply {
            // setDrawGridLines(true)
            addLimitLine(limitLine)
        }
        invalidate()
    }

    enum class LimitLineOffsetType(val offset: Int) {
        PAGES(offset = 10),
        BOOKS(offset = 1)
    }
}