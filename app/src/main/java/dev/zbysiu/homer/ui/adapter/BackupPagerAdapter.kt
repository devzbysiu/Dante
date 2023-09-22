package dev.zbysiu.homer.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dev.zbysiu.homer.R
import dev.zbysiu.homer.ui.fragment.BackupBackupFragment
import dev.zbysiu.homer.ui.fragment.BackupRestoreFragment

class BackupPagerAdapter(
    private val context: Context,
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> BackupBackupFragment.newInstance()
            1 -> BackupRestoreFragment.newInstance()
            else -> throw IllegalStateException("Index $position out of range in BackupPagerAdapter!")
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.backup_title)
            1 -> context.getString(R.string.restore_title)
            else -> "" // Never the case
        }
    }
}