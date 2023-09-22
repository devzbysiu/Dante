package dev.zbysiu.homer.core.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dev.zbysiu.homer.core.book.realm.RealmInstanceProvider
import dev.zbysiu.homer.core.data.BookEntityDao
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.data.DefaultBookRepository
import dev.zbysiu.homer.core.data.PageRecordDao
import dev.zbysiu.homer.core.data.ReadingGoalRepository
import dev.zbysiu.homer.core.data.local.DanteRealmMigration
import dev.zbysiu.homer.core.data.local.RealmBookEntityDao
import dev.zbysiu.homer.core.data.local.RealmPageRecordDao
import dev.zbysiu.homer.core.data.local.SharedPrefsBackedReadingGoalRepository
import dev.zbysiu.homer.core.image.GlideImageLoader
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.core.image.picker.DefaultImagePicking
import dev.zbysiu.homer.core.image.picker.ImagePickerConfig
import dev.zbysiu.homer.core.image.picker.ImagePicking
import dev.zbysiu.homer.core.network.BookDownloader
import dev.zbysiu.homer.core.network.DefaultDetailsDownloader
import dev.zbysiu.homer.core.network.DetailsDownloader
import dev.zbysiu.homer.core.network.google.BookDetailsApi
import dev.zbysiu.homer.core.network.google.GoogleBooksApi
import dev.zbysiu.homer.core.network.google.GoogleBooksDownloader
import dev.zbysiu.homer.core.user.FirebaseUserRepository
import dev.zbysiu.homer.core.user.UserRepository
import dev.zbysiu.homer.util.scheduler.AppSchedulerFacade
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

@Module
class CoreModule(
    private val app: Application,
    private val config: CoreModuleConfig
) {

    @Provides
    @Singleton
    @Named(LOCAL_BOOK_DAO)
    fun provideBookDao(realm: RealmInstanceProvider): BookEntityDao {
        return RealmBookEntityDao(realm)
    }

    @Provides
    @Singleton
    fun providePageRecordDao(realm: RealmInstanceProvider): PageRecordDao {
        return RealmPageRecordDao(realm)
    }

    @Provides
    @Singleton
    @Named(READING_GOAL_SHARED_PREFERENCES)
    fun provideReadingGoalSharedPreferences(): SharedPreferences {
        return app.getSharedPreferences(READING_GOAL_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideReadingGoalRepository(
        @Named(READING_GOAL_SHARED_PREFERENCES) sharedPreferences: SharedPreferences,
        schedulerFacade: SchedulerFacade
    ): ReadingGoalRepository {
        return SharedPrefsBackedReadingGoalRepository(sharedPreferences, schedulerFacade)
    }

    @Provides
    @Singleton
    fun provideBookRepository(
        @Named(LOCAL_BOOK_DAO) localBookDao: BookEntityDao
    ): BookRepository {
        return DefaultBookRepository(localBookDao = localBookDao)
    }

    @Provides
    @Singleton
    fun provideBookDownloader(
        api: GoogleBooksApi,
        schedulerFacade: SchedulerFacade
    ): BookDownloader {
        return GoogleBooksDownloader(api, schedulerFacade)
    }

    @Provides
    @Singleton
    fun provideDetailsDownloader(
        api: BookDetailsApi,
    ): DetailsDownloader {
        return DefaultDetailsDownloader(api)
    }

    @Provides
    @Singleton
    fun provideRealmInstanceProvider(): RealmInstanceProvider {
        return RealmInstanceProvider(
            RealmConfiguration.Builder()
                .schemaVersion(DanteRealmMigration.migrationVersion)
                .allowWritesOnUiThread(config.allowRealmExecutionOnUiThread)
                .allowQueriesOnUiThread(config.allowRealmExecutionOnUiThread)
                .migration(DanteRealmMigration())
                .build()
        )
    }

    @Provides
    @Singleton
    fun provideSchedulerFacade(): SchedulerFacade {
        return AppSchedulerFacade()
    }

    @Provides
    @Singleton
    fun provideImageLoader(): ImageLoader {
        return GlideImageLoader
    }

    @Provides
    @Singleton
    fun provideImagePicker(): ImagePicking {
        return DefaultImagePicking(
            ImagePickerConfig(
                maxSize = 1024,
                maxHeight = 1280,
                maxWidth = 720
            )
        )
    }

    @Provides
    fun provideUserRepository(
        fbAuth: FirebaseAuth,
        schedulers: SchedulerFacade
    ): UserRepository {
        return FirebaseUserRepository(fbAuth, schedulers)
    }

    companion object {

        private const val LOCAL_BOOK_DAO = "local_book_dao"
        private const val READING_GOAL_SHARED_PREFERENCES = "reading_goal_shared_preferences"
    }

    data class CoreModuleConfig(
        val allowRealmExecutionOnUiThread: Boolean
    )
}