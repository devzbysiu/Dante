package dev.zbysiu.homer.injection

import dev.zbysiu.homer.DanteApp
import dev.zbysiu.homer.core.injection.CoreComponent
import dev.zbysiu.homer.core.injection.ModuleScope
import dev.zbysiu.homer.core.injection.NetworkModule
import dev.zbysiu.homer.ui.activity.BookStorageActivity
import dev.zbysiu.homer.ui.activity.DetailActivity
import dev.zbysiu.homer.ui.activity.LoginActivity
import dev.zbysiu.homer.ui.activity.MainActivity
import dev.zbysiu.homer.ui.activity.ManualAddActivity
import dev.zbysiu.homer.ui.activity.NotesActivity
import dev.zbysiu.homer.ui.activity.SearchActivity
import dev.zbysiu.homer.ui.fragment.AnnouncementFragment
import dev.zbysiu.homer.ui.fragment.BackupBackupFragment
import dev.zbysiu.homer.ui.fragment.BackupFragment
import dev.zbysiu.homer.ui.fragment.BackupRestoreFragment
import dev.zbysiu.homer.ui.fragment.BookDetailFragment
import dev.zbysiu.homer.ui.fragment.FeatureFlagConfigFragment
import dev.zbysiu.homer.ui.fragment.ImportBooksStorageFragment
import dev.zbysiu.homer.ui.fragment.LabelCategoryBottomSheetFragment
import dev.zbysiu.homer.ui.fragment.LabelPickerBottomSheetFragment
import dev.zbysiu.homer.ui.fragment.LoginFragment
import dev.zbysiu.homer.ui.fragment.MailLoginBottomSheetDialogFragment
import dev.zbysiu.homer.ui.fragment.MainBookFragment
import dev.zbysiu.homer.ui.fragment.ManualAddFragment
import dev.zbysiu.homer.ui.fragment.MenuFragment
import dev.zbysiu.homer.ui.fragment.OnlineStorageFragment
import dev.zbysiu.homer.ui.fragment.PageRecordsDetailFragment
import dev.zbysiu.homer.ui.fragment.PickRandomBookFragment
import dev.zbysiu.homer.ui.fragment.RateFragment
import dev.zbysiu.homer.ui.fragment.SearchFragment
import dev.zbysiu.homer.ui.fragment.SettingsFragment
import dev.zbysiu.homer.ui.fragment.StatisticsFragment
import dev.zbysiu.homer.ui.fragment.SuggestBookBottomSheetDialogFragment
import dev.zbysiu.homer.ui.fragment.SuggestionsFragment
import dev.zbysiu.homer.ui.fragment.TimeLineFragment
import dev.zbysiu.homer.ui.widget.DanteAppWidget
import dev.zbysiu.homer.ui.widget.DanteRemoteViewsService
import dagger.Component

/**
 * Author:  Martin Macheiner
 * Date:    19.01.2017
 */
@Component(
    modules = [
        (NetworkModule::class),
        (AppModule::class),
        (AppNetworkModule::class),
        (ViewModelModule::class),
        (FirebaseModule::class),
        (BookStorageModule::class)
    ],
    dependencies = [CoreComponent::class]
)
@ModuleScope
interface AppComponent {

    fun inject(app: DanteApp)

    fun inject(activity: MainActivity)

    fun inject(activity: DetailActivity)

    fun inject(activity: SearchActivity)

    fun inject(activity: BookStorageActivity)

    fun inject(activity: LoginActivity)

    fun inject(activity: NotesActivity)

    fun inject(activity: ManualAddActivity)

    fun inject(fragment: MainBookFragment)

    fun inject(fragment: BackupFragment)

    fun inject(fragment: BackupBackupFragment)

    fun inject(fragment: BackupRestoreFragment)

    fun inject(fragment: SearchFragment)

    fun inject(fragment: SuggestionsFragment)

    fun inject(fragment: BookDetailFragment)

    fun inject(fragment: MenuFragment)

    fun inject(fragment: ManualAddFragment)

    fun inject(fragment: StatisticsFragment)

    fun inject(fragment: SettingsFragment)

    fun inject(fragment: RateFragment)

    fun inject(fragment: LoginFragment)

    fun inject(fragment: FeatureFlagConfigFragment)

    fun inject(fragment: AnnouncementFragment)

    fun inject(fragment: TimeLineFragment)

    fun inject(dialogFragment: MailLoginBottomSheetDialogFragment)

    fun inject(danteAppWidget: DanteAppWidget)

    fun inject(remoteViewsService: DanteRemoteViewsService)

    fun inject(fragment: LabelPickerBottomSheetFragment)

    fun inject(fragment: SuggestBookBottomSheetDialogFragment)

    fun inject(labelCategoryBottomSheetFragment: LabelCategoryBottomSheetFragment)

    fun inject(fragment: OnlineStorageFragment)

    fun inject(fragment: ImportBooksStorageFragment)

    fun inject(fragment: PickRandomBookFragment)

    fun inject(fragment: PageRecordsDetailFragment)
}
