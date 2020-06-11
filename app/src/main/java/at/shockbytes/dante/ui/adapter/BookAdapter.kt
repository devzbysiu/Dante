package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.ColorUtils.desaturateAndDevalue
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.isNightModeEnabled
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.view.BookDiffUtilCallback
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import com.google.android.material.chip.Chip
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book.*
import java.util.Collections

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 */
class BookAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    private val onOverflowActionClickedListener: (BookEntity) -> Unit,
    private val onLabelClickedListener: (BookLabel) -> Unit,
    onItemClickListener: OnItemClickListener<BookEntity>,
    onItemMoveListener: OnItemMoveListener<BookEntity>
) : BaseAdapter<BookEntity>(
    context,
    onItemClickListener = onItemClickListener,
    onItemMoveListener = onItemMoveListener
), ItemTouchHelperAdapter {

    init {
        setHasStableIds(false)
    }

    override fun getItemId(position: Int): Long {
        return data[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<BookEntity> {
        return ViewHolder(inflater.inflate(R.layout.item_book, parent, false))
    }

    override fun onItemDismiss(position: Int) {
        val removed = data.removeAt(position)
        onItemMoveListener?.onItemDismissed(removed, position)
    }

    override fun onItemMove(from: Int, to: Int): Boolean {

        // Switch the item within the collection
        if (from < to) {
            for (i in from until to) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
        onItemMoveListener?.onItemMove(data[from], from, to)

        return true
    }

    override fun onItemMoveFinished() {
        onItemMoveListener?.onItemMoveFinished()
    }

    fun updateData(books: List<BookEntity>) {
        val diffResult = DiffUtil.calculateDiff(BookDiffUtilCallback(data, books))

        data.clear()
        data.addAll(books)

        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<BookEntity>(containerView), LayoutContainer {

        override fun bindToView(content: BookEntity, position: Int) {
            updateTexts(content)
            updateImageThumbnail(content.thumbnailAddress)
            updateProgress(content)
            updateLabels(content.labels)
            setOverflowClickListener(content)
        }

        private fun updateLabels(labels: List<BookLabel>) {
            chips_item_book_label.removeAllViews()

            val isNightModeEnabled = context.isNightModeEnabled()

            labels
                .map { label ->
                    buildChipViewFromLabel(label, isNightModeEnabled)
                }
                .forEach(chips_item_book_label::addView)
        }

        private fun buildChipViewFromLabel(label: BookLabel, isNightModeEnabled: Boolean): Chip {

            val chipColor = if (isNightModeEnabled) {
                desaturateAndDevalue(Color.parseColor(label.hexColor), by = 0.25f)
            } else {
                Color.parseColor(label.hexColor)
            }

            return Chip(containerView.context).apply {
                chipBackgroundColor = ColorStateList.valueOf(chipColor)
                text = label.title
                setTextColor(Color.WHITE)
                setOnClickListener {
                    onLabelClickedListener(label)
                }
            }
        }

        private fun setOverflowClickListener(content: BookEntity) {
            item_book_img_overflow.setOnClickListener {
                onOverflowActionClickedListener(content)
            }
        }

        private fun updateProgress(t: BookEntity) {

            val showProgress = t.reading && t.hasPages

            if (showProgress) {
                val progress = DanteUtils.computePercentage(
                    t.currentPage.toDouble(),
                    t.pageCount.toDouble()
                )
                animateBookProgress(progress)
                item_book_tv_progress.text = context.getString(R.string.percentage_formatter, progress)
            }

            item_book_group_progress.setVisible(showProgress)
        }

        private fun animateBookProgress(progress: Int) {
            item_book_pb.progress = progress
        }

        private fun updateImageThumbnail(address: String?) {
            if (!address.isNullOrEmpty()) {
                imageLoader.loadImageWithCornerRadius(
                    context,
                    address,
                    item_book_img_thumb,
                    cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                )
            } else {
                // Books with no image will recycle another cover if not cleared here
                item_book_img_thumb.setImageResource(R.drawable.ic_placeholder)
            }
        }

        private fun updateTexts(t: BookEntity) {
            item_book_txt_title.text = t.title
            item_book_txt_author.text = t.author
            item_book_txt_subtitle.apply {
                text = t.subTitle
                setVisible(t.subTitle.isNotEmpty())
            }
        }
    }
}