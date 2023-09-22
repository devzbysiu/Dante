package dev.zbysiu.homer.ui.adapter.pagerecords

import android.content.Context
import android.view.ViewGroup
import dev.zbysiu.homer.core.book.PageRecord
import dev.zbysiu.homer.databinding.ItemPageRecordsDetailBinding
import dev.zbysiu.homer.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter

class PageRecordsAdapter(
    context: Context,
    private val onItemDeletedListener: (PageRecord) -> Unit
) : BaseAdapter<PageRecordDetailItem>(context) {

    fun updateData(updatedData: List<PageRecordDetailItem>) {

        data.clear()
        data.addAll(updatedData)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<PageRecordDetailItem> {
        val vb = ItemPageRecordsDetailBinding.inflate(parent.context.layoutInflater(), parent, false)
        return PageRecordsViewHolder(vb)
    }

    inner class PageRecordsViewHolder(
        private val vb: ItemPageRecordsDetailBinding
    ) : BaseAdapter.ViewHolder<PageRecordDetailItem>(vb.root) {

        override fun bindToView(content: PageRecordDetailItem, position: Int) {
            with(content) {
                vb.tvItemPageRecordsDetailDate.text = formattedDate
                vb.tvItemPageRecordsDetailPages.text = formattedPagesRead

                vb.btnItemPageRecordsDetailDelete.setOnClickListener {
                    onItemDeletedListener(pageRecord)
                }
            }
        }
    }
}