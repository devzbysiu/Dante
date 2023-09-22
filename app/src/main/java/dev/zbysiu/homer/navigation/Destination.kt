package dev.zbysiu.homer.navigation

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import dev.zbysiu.homer.R
import dev.zbysiu.homer.camera.BarcodeCaptureActivity
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.core.book.BookId
import dev.zbysiu.homer.core.createSharingIntent
import dev.zbysiu.homer.ui.activity.BookStorageActivity
import dev.zbysiu.homer.ui.activity.DetailActivity
import dev.zbysiu.homer.ui.activity.LoginActivity
import dev.zbysiu.homer.ui.activity.MainActivity
import dev.zbysiu.homer.ui.activity.ManualAddActivity
import dev.zbysiu.homer.ui.activity.NotesActivity
import dev.zbysiu.homer.ui.activity.SearchActivity
import dev.zbysiu.homer.ui.activity.SettingsActivity
import dev.zbysiu.homer.ui.activity.StatisticsActivity
import dev.zbysiu.homer.ui.activity.SuggestionsActivity
import dev.zbysiu.homer.ui.activity.TimeLineActivity
import dev.zbysiu.homer.ui.activity.WishlistActivity
import kotlinx.parcelize.Parcelize

sealed class Destination {

    abstract fun provideIntent(context: Context): Intent

    data class BookDetail(private val info: BookDetailInfo) : Destination() {

        @Parcelize
        data class BookDetailInfo(
            val id: BookId,
            val title: String
        ) : Parcelable

        override fun provideIntent(context: Context): Intent {
            return DetailActivity.newIntent(context, info.id, info.title)
        }
    }

    data class Share(private val bookEntity: BookEntity) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return Intent.createChooser(
                bookEntity.createSharingIntent(context),
                context.resources.getText(R.string.send_to)
            )
        }
    }

    data class Main(
        private val bookDetailInfo: BookDetail.BookDetailInfo? = null,
        private val openCameraAfterLaunch: Boolean = false
    ) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return MainActivity.newIntent(context, bookDetailInfo, openCameraAfterLaunch)
        }
    }

    object Search : Destination() {

        override fun provideIntent(context: Context): Intent {
            return SearchActivity.newIntent(context)
        }
    }

    data class ManualAdd(private val updatedBookEntity: BookEntity? = null) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return ManualAddActivity.newIntent(context, updatedBookEntity)
        }
    }

    object Statistics : Destination() {

        override fun provideIntent(context: Context): Intent {
            return StatisticsActivity.newIntent(context)
        }
    }

    object Timeline : Destination() {

        override fun provideIntent(context: Context): Intent {
            return TimeLineActivity.newIntent(context)
        }
    }

    object BookStorage : Destination() {

        override fun provideIntent(context: Context): Intent {
            return BookStorageActivity.newIntent(context)
        }
    }

    object Settings : Destination() {

        override fun provideIntent(context: Context): Intent {
            return SettingsActivity.newIntent(context)
        }
    }

    object BarcodeScanner : Destination() {

        override fun provideIntent(context: Context): Intent {
            return BarcodeCaptureActivity.newIntent(context)
        }
    }

    data class Notes(private val notesBundle: NotesBundle) : Destination() {

        override fun provideIntent(context: Context): Intent {
            return NotesActivity.newIntent(context, notesBundle)
        }
    }

    object Wishlist : Destination() {
        override fun provideIntent(context: Context): Intent {
            return WishlistActivity.newIntent(context)
        }
    }

    object Suggestions : Destination() {
        override fun provideIntent(context: Context): Intent {
            return SuggestionsActivity.newIntent(context)
        }
    }

    object Login : Destination() {
        override fun provideIntent(context: Context): Intent {
            return LoginActivity.newIntent(context)
        }
    }
}
