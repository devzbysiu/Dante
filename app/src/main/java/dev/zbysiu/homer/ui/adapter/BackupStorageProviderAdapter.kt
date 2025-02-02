package dev.zbysiu.homer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.ui.custom.BackupStorageProviderView
import at.shockbytes.util.adapter.BaseAdapter

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
class BackupStorageProviderAdapter(
    context: Context,
    onItemClickListener: OnItemClickListener<BackupStorageProvider>
) : BaseAdapter<BackupStorageProvider>(context, onItemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BackupStorageProviderView(context))
    }

    fun updateData(providers: List<BackupStorageProvider>) {
        data.clear()
        data.addAll(providers)
    }

    inner class ViewHolder(
        val containerView: BackupStorageProviderView
    ) : BaseAdapter.ViewHolder<BackupStorageProvider>(containerView) {

        override fun bindToView(content: BackupStorageProvider, position: Int) {
            containerView.setStorageProvider(content) {
                onItemClickListener?.onItemClick(content, adapterPosition, containerView)
            }
        }
    }
}
