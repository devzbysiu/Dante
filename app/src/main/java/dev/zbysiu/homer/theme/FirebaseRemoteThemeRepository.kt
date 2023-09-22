package dev.zbysiu.homer.theme

import dev.zbysiu.homer.theme.data.RemoteSeasonalTheme
import dev.zbysiu.homer.util.singleOf
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dev.zbysiu.homer.util.fromJson
import io.reactivex.Single

class FirebaseRemoteThemeRepository(
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) : ThemeRepository {

    override fun getSeasonalTheme(): Single<SeasonalTheme> {
        return singleOf { remoteConfig.getString(RESOURCE_KEY_THEME) }
            .map { json ->
                gson.fromJson<RemoteSeasonalTheme>(json).toSeasonalTheme()
            }
    }

    private fun RemoteSeasonalTheme.toSeasonalTheme(): SeasonalTheme {
        return when (type) {
            SeasonalTheme.RESOURCE_TYPE_LOTTIE_ASSETS -> {
                SeasonalTheme.LottieAssetsTheme(lottieAsset = resource, lottieSpeed = resourceSpeed)
            }
            else -> SeasonalTheme.NoTheme
        }
    }

    companion object {
        private const val RESOURCE_KEY_THEME = "seasonal_theme"
    }
}