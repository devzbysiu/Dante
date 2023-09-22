package dev.zbysiu.homer.ui.adapter.main

import android.view.ViewGroup
import dev.zbysiu.homer.databinding.ItemRandomPickBinding
import dev.zbysiu.homer.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter

class RandomPickViewHolder(
    private val vb: ItemRandomPickBinding,
    private val callback: RandomPickCallback?
) : BaseAdapter.ViewHolder<BookAdapterItem>(vb.root) {

    override fun bindToView(content: BookAdapterItem, position: Int) {

        vb.btnItemRandomPick.setOnClickListener {
            callback?.onRandomPickClicked()
        }

        vb.ivItemRandomPickDismiss.setOnClickListener {
            callback?.onDismiss()
        }
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            callback: RandomPickCallback?
        ): RandomPickViewHolder {
            return RandomPickViewHolder(
                ItemRandomPickBinding.inflate(parent.context.layoutInflater(), parent, false),
                callback
            )
        }
    }
}