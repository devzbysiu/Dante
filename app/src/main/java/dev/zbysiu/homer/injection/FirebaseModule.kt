package dev.zbysiu.homer.injection

import android.content.Context
import dev.zbysiu.homer.BuildConfig
import dev.zbysiu.homer.R
import dev.zbysiu.homer.storage.FirebaseImageUploadStorage
import dev.zbysiu.homer.storage.ImageUploadStorage
import dev.zbysiu.tracking.DebugTracker
import dev.zbysiu.tracking.FirebaseTracker
import dev.zbysiu.tracking.Tracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2018
 */
@Module
class FirebaseModule(private val context: Context) {

    /**
     * Do not use remote config at the moment since it is not
     * used anyways and only leads to sporadic crashes.
     */
    // @Provides
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(getFetchInterval())
            .build()
        return FirebaseRemoteConfig.getInstance()
            .apply {
                setConfigSettingsAsync(configSettings).addOnCompleteListener {
                    Timber.d("Firebase Config settings set")
                }
                setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
                    Timber.d("Firebase defaults set")
                }

                try {
                    fetchAndActivate()
                        .addOnFailureListener { exception ->
                            Timber.e(exception)
                        }
                        .addOnSuccessListener { isActivated ->
                            Timber.d("FirebaseRemoteConfig fetched and activated: $isActivated")
                        }
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            }
    }

    /**
     * If in debug mode, always fetch latest remote config values
     */
    private fun getFetchInterval(): Long {
        return if (BuildConfig.DEBUG) 0 else DEFAULT_MINIMUM_FETCH_INTERVAL_IN_SECONDS
    }

    @Provides
    fun provideTracker(): Tracker {
        return if (BuildConfig.DEBUG) {
            DebugTracker()
        } else {
            FirebaseTracker(context)
        }
    }

    @Provides
    fun provideImageUploadStorage(fbAuth: FirebaseAuth): ImageUploadStorage {
        return FirebaseImageUploadStorage(fbAuth)
    }

    companion object {
        private const val DEFAULT_MINIMUM_FETCH_INTERVAL_IN_SECONDS = 259200L // = 3 days
    }
}