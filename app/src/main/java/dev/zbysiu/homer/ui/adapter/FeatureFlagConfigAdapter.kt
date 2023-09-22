package dev.zbysiu.homer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import dev.zbysiu.homer.databinding.ItemFeatureFlagBinding
import dev.zbysiu.homer.flagging.FeatureFlagItem
import dev.zbysiu.homer.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter

class FeatureFlagConfigAdapter(
    context: Context,
    private val onItemChangedListener: ((item: FeatureFlagItem) -> Unit)
) : BaseAdapter<FeatureFlagItem>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(ItemFeatureFlagBinding.inflate(context.layoutInflater(), parent, false))
    }

    inner class ViewHolder(
        private val vb: ItemFeatureFlagBinding
    ) : BaseAdapter.ViewHolder<FeatureFlagItem>(vb.root) {

        override fun bindToView(content: FeatureFlagItem, position: Int) {
            with(content) {

                vb.itemFeatureFlagTxtTitle.text = displayName
                vb.itemFeatureFlagSwitch.isChecked = value
            }

            vb.itemFeatureFlagRoot.setOnClickListener {
                vb.itemFeatureFlagSwitch.toggle()
                updateItemState(content)
            }

            vb.itemFeatureFlagSwitch.setOnClickListener {
                updateItemState(content)
            }
        }

        private fun updateItemState(item: FeatureFlagItem) {

            val position = getLocation(item)
            if (position > -1) {
                val updatedItem = data[position].copy(value = !data[position].value)
                data[position] = updatedItem
                onItemChangedListener.invoke(updatedItem)
                notifyItemChanged(position)
            }
        }
    }
}