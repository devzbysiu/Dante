package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_backup_entry.*
import kotterknife.bindView

/**
 * Author:  Martin Macheiner
 * Date:    22.04.2017.
 */
class BackupEntryAdapter(cxt: Context) : BaseAdapter<BackupEntry>(cxt), ItemTouchHelperAdapter {

    var onItemDeleteClickListener: ((BackupEntry, Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter<BackupEntry>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_backup_entry, parent, false))
    }

    override fun onItemMove(from: Int, to: Int) = false

    override fun onItemMoveFinished() {}

    override fun onItemDismiss(position: Int) {
        onItemMoveListener?.onItemDismissed(data[position], position)
    }

    inner class ViewHolder(
        override val containerView: View
    ) : BaseAdapter<BackupEntry>.ViewHolder(containerView), LayoutContainer {

        override fun bindToView(t: BackupEntry) {

            if (t.storageProvider == "gdrive") {
                item_backup_entry_imgview_provider.setImageResource(R.drawable.ic_google_drive)
            }

            item_backup_entry_txt_time.text = DanteUtils.formatTimestamp(t.timestamp)
            item_backup_entry_txt_books.text = context.getString(R.string.backup_books_amount, t.books)
            item_backup_entry_txt_device.text = t.device

            item_backup_entry_btn_delete.setOnClickListener {
                onItemDeleteClickListener?.invoke(t, getLocation(t))
            }
        }
    }
}
