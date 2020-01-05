package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_backup_entry.*

/**
 * Author:  Martin Macheiner
 * Date:    22.04.2017
 */
class BackupEntryAdapter(
    ctx: Context,
    onItemClickListener: OnItemClickListener<BackupMetadataState>
) : BaseAdapter<BackupMetadataState>(ctx, onItemClickListener), ItemTouchHelperAdapter {

    var onItemDeleteClickListener: ((BackupMetadata, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BackupMetadataState> {
        return BackupViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false))
    }

    override fun onItemMove(from: Int, to: Int) = false

    override fun onItemMoveFinished() = Unit

    override fun onItemDismiss(position: Int) {
        onItemMoveListener?.onItemDismissed(data[position], position)
    }

    fun updateData(backupStates: List<BackupMetadataState>) {
        data.clear()
        data.addAll(backupStates)
    }

    inner class BackupViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<BackupMetadataState>(containerView), LayoutContainer {

        override fun bindToView(content: BackupMetadataState, position: Int) {

            with(content.entry) {
                item_backup_entry_imgview_provider.setImageResource(storageProvider.icon)

                item_backup_entry_txt_time.text = DanteUtils.formatTimestamp(timestamp)
                item_backup_entry_txt_books.text = context.getString(R.string.backup_books_amount, books)
                item_backup_entry_txt_device.text = device

                if (content is BackupMetadataState.Active) {

                    item_backup_entry_card.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    item_backup_entry_btn_delete.setVisible(true)
                    item_backup_entry_btn_delete.setOnClickListener {
                        onItemDeleteClickListener?.invoke(this, getLocation(content))
                    }
                } else {
                    item_backup_entry_card.setBackgroundColor(ContextCompat.getColor(context, R.color.disabled_view))
                    item_backup_entry_btn_delete.visibility = View.INVISIBLE
                    item_backup_entry_btn_delete.setOnClickListener(null)
                }
            }
        }
    }
}
