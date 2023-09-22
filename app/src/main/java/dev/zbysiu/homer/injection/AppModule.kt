package dev.zbysiu.homer.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dev.zbysiu.homer.announcement.AnnouncementProvider
import dev.zbysiu.homer.announcement.SharedPrefsAnnouncementProvider
import dev.zbysiu.homer.core.login.LoginRepository
import dev.zbysiu.homer.util.settings.DanteSettings
import dev.zbysiu.homer.flagging.FeatureFlagging
import dev.zbysiu.homer.flagging.FirebaseFeatureFlagging
import dev.zbysiu.homer.flagging.SharedPreferencesFeatureFlagging
import dev.zbysiu.homer.suggestions.SuggestionsRepository
import dev.zbysiu.homer.suggestions.cache.DataStoreSuggestionsCache
import dev.zbysiu.homer.suggestions.cache.SuggestionsCache
import dev.zbysiu.homer.suggestions.firebase.FirebaseSuggestionsApi
import dev.zbysiu.homer.suggestions.firebase.FirebaseSuggestionsRepository
import dev.zbysiu.homer.theme.NoOpThemeRepository
import dev.zbysiu.homer.theme.ThemeRepository
import dev.zbysiu.homer.util.explanations.Explanations
import dev.zbysiu.homer.util.explanations.SharedPrefsExplanations
import dev.zbysiu.homer.util.permission.AndroidPermissionManager
import dev.zbysiu.homer.util.permission.PermissionManager
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.tracking.Tracker
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
@Module
class AppModule(private val app: Application) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    fun provideDanteSettings(
        sharedPreferences: SharedPreferences,
        schedulers: SchedulerFacade
    ): DanteSettings {
        return DanteSettings(app.applicationContext, sharedPreferences, schedulers)
    }

    @Provides
    fun providePermissionManager(): PermissionManager {
        return AndroidPermissionManager()
    }

    @Provides
    fun provideFeatureFlagging(): FeatureFlagging {
        /**
         * Do not use [FirebaseFeatureFlagging] since there are no remotely controlled feature flags.
         */
        val prefs = app.getSharedPreferences("feature_flagging", Context.MODE_PRIVATE)
        return SharedPreferencesFeatureFlagging(prefs)
    }

    @Provides
    fun provideAnnouncementProvider(): AnnouncementProvider {
        val prefs = app.getSharedPreferences("announcements", Context.MODE_PRIVATE)
        return SharedPrefsAnnouncementProvider(prefs)
    }

    @Provides
    fun provideSuggestionCache(): SuggestionsCache {
        return DataStoreSuggestionsCache(app.applicationContext, Gson())
    }

    @Provides
    fun provideSuggestionsRepository(
        firebaseSuggestionsApi: FirebaseSuggestionsApi,
        schedulerFacade: SchedulerFacade,
        loginRepository: LoginRepository,
        suggestionsCache: SuggestionsCache,
        tracker: Tracker
    ): SuggestionsRepository {
        return FirebaseSuggestionsRepository(
            firebaseSuggestionsApi,
            schedulerFacade,
            loginRepository,
            suggestionsCache,
            tracker
        )
    }

    @Provides
    fun provideThemeRepository(): ThemeRepository {
        return NoOpThemeRepository
    }

    @Provides
    fun provideExplanations(): Explanations {
        val sharedPreferences = app.getSharedPreferences("preferences_explanations", Context.MODE_PRIVATE)
        return SharedPrefsExplanations(sharedPreferences)
    }
}
