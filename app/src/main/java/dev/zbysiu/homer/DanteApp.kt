package dev.zbysiu.homer

import android.os.StrictMode
import androidx.multidex.MultiDexApplication
import androidx.appcompat.app.AppCompatDelegate
import dev.zbysiu.homer.core.injection.CoreComponent
import dev.zbysiu.homer.core.injection.CoreComponentProvider
import dev.zbysiu.homer.core.injection.CoreModule
import dev.zbysiu.homer.core.injection.DaggerCoreComponent
import dev.zbysiu.homer.core.injection.NetworkModule
import dev.zbysiu.homer.injection.AppComponent
import dev.zbysiu.homer.injection.AppModule
import dev.zbysiu.homer.injection.AppNetworkModule
import dev.zbysiu.homer.injection.BookStorageModule
import dev.zbysiu.homer.injection.DaggerAppComponent
import dev.zbysiu.homer.injection.FirebaseModule
import dev.zbysiu.homer.core.injection.LoginModule
import dev.zbysiu.homer.util.CrashlyticsReportingTree
import dev.zbysiu.homer.util.settings.DanteSettings
import dev.zbysiu.tracking.Tracker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.plugins.RxJavaPlugins
import io.realm.Realm
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class DanteApp : MultiDexApplication(), CoreComponentProvider {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val coreComponent: CoreComponent by lazy {
        DaggerCoreComponent.builder()
            .coreModule(
                CoreModule(
                    app = this,
                    config = CoreModule.CoreModuleConfig(allowRealmExecutionOnUiThread = true)
                )
            )
            .loginModule(LoginModule(this))
            .networkModule(NetworkModule())
            .build()
    }

    lateinit var appComponent: AppComponent
        private set

    @Inject
    lateinit var danteSettings: DanteSettings

    @Inject
    lateinit var tracker: Tracker

    override fun onCreate() {
        super.onCreate()
        setStrictMode()

        appComponent = DaggerAppComponent.builder()
            .appNetworkModule(AppNetworkModule())
            .appModule(AppModule(this))
            .firebaseModule(FirebaseModule(this))
            .bookStorageModule(BookStorageModule(this))
            .coreComponent(provideCoreComponent())
            .build()
            .also { component ->
                component.inject(this)
            }

        Realm.init(this)
        JodaTimeAndroid.init(this)

        configureCrashlytics()
        configureLogging()
        configureRxJavaErrorHandling()
        configureTracker()
    }

    override fun provideCoreComponent(): CoreComponent {
        return coreComponent
    }

    private fun configureRxJavaErrorHandling() {
        RxJavaPlugins.setErrorHandler { throwable ->
            Timber.e(throwable)
        }
    }

    private fun configureTracker() {
        tracker.isTrackingAllowed = danteSettings.trackingEnabled
    }

    private fun configureLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsReportingTree())
        }
    }

    private fun configureCrashlytics() {

        // Only enable crash collection data in release mode
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }

        // to catch and send crash report to crashlytics when app crashes
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Timber.e(e, "uncaught exception")
            defaultExceptionHandler?.uncaughtException(t, e)
        }
    }

    private fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build())
        }
    }
}
