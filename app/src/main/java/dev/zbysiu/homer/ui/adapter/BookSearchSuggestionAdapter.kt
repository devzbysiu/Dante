package dev.zbysiu.homer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookIds
import dev.zbysiu.homer.core.book.BookSearchItem
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.databinding.ItemBookSearchSuggestionBinding
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

/**
 * Author: Martin Macheiner
 * Date: 03.02.2018.
 */
class BookSearchSuggestionAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    private val addClickedListener: (BookSearchItem) -> Unit,
    private val deleteClickedListener: (BookSearchItem) -> Unit,
    onItemClickListener: OnItemClickListener<BookSearchItem>
) : BaseAdapter<BookSearchItem>(context, onItemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBookSearchSuggestionBinding.inflate(inflater, parent, false))
    }

    inner class ViewHolder(
        private val vb: ItemBookSearchSuggestionBinding
    ) : BaseAdapter.ViewHolder<BookSearchItem>(vb.root) {

        override fun bindToView(content: BookSearchItem, position: Int) {
            vb.itemBookSearchSuggestionTxtTitle.text = content.title
            vb.itemBookSearchSuggestionTxtAuthor.text = content.author

            vb.itemBookSearchSuggestionBtnAdd.apply {
                setVisible(BookIds.isInvalid(content.bookId))
                setOnClickListener {
                    addClickedListener.invoke(content)
                }
            }

            vb.itemBookSearchSuggestionBtnDelete.apply {
                setVisible(BookIds.isValid(content.bookId))
                setOnClickListener {
                    deleteClickedListener.invoke(content)
                }
            }

            loadImage(content.thumbnailAddress)
        }

        private fun loadImage(thumbnailAddress: String?) {
            if (!thumbnailAddress.isNullOrEmpty()) {
                imageLoader.loadImageWithCornerRadius(
                    context,
                    thumbnailAddress,
                    vb.itemBookSearchSuggestionImgviewCover,
                    R.drawable.ic_placeholder,
                    cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner)
                        .toInt()
                )
            }
        }
    }
}