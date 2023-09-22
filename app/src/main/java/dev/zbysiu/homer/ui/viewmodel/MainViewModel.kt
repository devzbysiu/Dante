package dev.zbysiu.homer.ui.viewmodel

import dev.zbysiu.homer.announcement.AnnouncementProvider
import dev.zbysiu.homer.theme.SeasonalTheme
import dev.zbysiu.homer.theme.ThemeRepository
import dev.zbysiu.homer.util.ExceptionHandlers
import dev.zbysiu.homer.util.addTo
import dev.zbysiu.homer.util.settings.DanteSettings
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2018
 */
class MainViewModel @Inject constructor(
    private val announcementProvider: AnnouncementProvider,
    private val danteSettings: DanteSettings,
    private val themeRepository: ThemeRepository
) : BaseViewModel() {

    sealed class MainEvent {

        object Announcement : MainEvent()
    }

    private val eventSubject = PublishSubject.create<MainEvent>()
    fun onMainEvent(): Observable<MainEvent> = eventSubject

    private val seasonalThemeSubject = BehaviorSubject.create<SeasonalTheme>()
    fun getSeasonalTheme(): Observable<SeasonalTheme> = seasonalThemeSubject
        .delay(2, TimeUnit.SECONDS)
        .distinctUntilChanged()

    fun requestSeasonalTheme() {
        themeRepository.getSeasonalTheme()
            .doOnError { seasonalThemeSubject.onNext(SeasonalTheme.NoTheme) }
            .subscribe(seasonalThemeSubject::onNext, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun queryAnnouncements() {
        val hasActiveAnnouncement = announcementProvider.getActiveAnnouncement() != null
        // Do not show announcements if the user first logs into the app,
        // even though there would be a new announcement
        val showAnnouncement = hasActiveAnnouncement && !danteSettings.isFirstUserSession
        if (showAnnouncement) {
            eventSubject.onNext(MainEvent.Announcement)
        }
    }
}