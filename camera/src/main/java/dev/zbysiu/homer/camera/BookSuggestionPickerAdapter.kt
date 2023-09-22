package dev.zbysiu.homer.camera

import android.content.Context
import android.view.ViewGroup
import dev.zbysiu.homer.camera.databinding.SuggestionPickerItemBinding
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.image.ImageLoader
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter

class BookSuggestionPickerAdapter(
    context: Context,
    suggestions: List<BookEntity>,
    private val imageLoader: ImageLoader,
    onItemClickListener: OnItemClickListener<BookEntity>
) : BaseAdapter<BookEntity>(context, onItemClickListener) {

    init {
        data = suggestions.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BookEntity> {
        val binding = SuggestionPickerItemBinding.inflate(inflater, parent, false)
        return SuggestionViewHolder(binding)
    }

    inner class SuggestionViewHolder(
        private val vb: SuggestionPickerItemBinding
    ) : BaseAdapter.ViewHolder<BookEntity>(vb.root) {

        override fun bindToView(content: BookEntity, position: Int) {
            with(content) {
                vb.tvSuggestionPickerItemTitle.text = title
                vb.tvSuggestionPickerItemAuthor.text = author

                thumbnailAddress?.let { imageUrl ->
                    imageLoader.loadImageWithCornerRadius(
                        context,
                        imageUrl,
                        vb.ivSuggestionPickerItemCover,
                        cornerDimension = AppUtils.convertDpInPixel(6, context)
                    )
                }
            }
        }
    }
}
