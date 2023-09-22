package dev.zbysiu.homer.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.databinding.BackupStorageProviderViewBinding
import dev.zbysiu.homer.util.Stability
import dev.zbysiu.homer.util.setVisible

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupStorageProviderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val vb = BackupStorageProviderViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    fun setStorageProvider(backupStorageProvider: BackupStorageProvider, click: ((BackupStorageProvider) -> Unit)? = null) {
        with(backupStorageProvider) {
            vb.ivBackupStorageProviderIcon.setImageResource(icon)
            vb.tvBackupStorageProviderTitle.text = title
            vb.tvBackupStorageProviderRationale.setText(rationale)

            vb.rootBackupStorageProvider.setOnClickListener { click?.invoke(this) }
            vb.tvBackupItemBeta.setVisible(backupStorageProvider.stability == Stability.BETA)
            vb.tvBackupItemDiscontinued.setVisible(backupStorageProvider.stability == Stability.DISCONTINUED)
        }
    }
}