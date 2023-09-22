package dev.zbysiu.homer.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dev.zbysiu.homer.R
import dev.zbysiu.homer.core.book.BookState
import dev.zbysiu.homer.ui.fragment.MainBookFragment

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2016
 */
class BookPagerAdapter(
    private val context: Context,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return MainBookFragment.newInstance(BookState.values()[position])
    }

    override fun getCount(): Int {
        return COUNT_STANDARD
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.tab_upcoming)
            1 -> context.getString(R.string.tab_current)
            2 -> context.getString(R.string.tab_done)
            else -> throw IllegalStateException("Invalid page position $position in BookPagerAdapter!")
        }
    }

    companion object {
        private const val COUNT_STANDARD = 3
    }
}
