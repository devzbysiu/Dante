package dev.zbysiu.homer.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.zbysiu.homer.ui.viewmodel.AnnouncementViewModel
import dev.zbysiu.homer.ui.viewmodel.BackupViewModel
import dev.zbysiu.homer.ui.viewmodel.BookDetailViewModel
import dev.zbysiu.homer.ui.viewmodel.BookListViewModel
import dev.zbysiu.homer.ui.viewmodel.FeatureFlagConfigViewModel
import dev.zbysiu.homer.ui.viewmodel.ImportBooksStorageViewModel
import dev.zbysiu.homer.ui.viewmodel.LabelCategoryViewModel
import dev.zbysiu.homer.ui.viewmodel.LabelManagementViewModel
import dev.zbysiu.homer.ui.viewmodel.LoginViewModel
import dev.zbysiu.homer.ui.viewmodel.MailLoginViewModel
import dev.zbysiu.homer.ui.viewmodel.MainViewModel
import dev.zbysiu.homer.ui.viewmodel.ManualAddViewModel
import dev.zbysiu.homer.ui.viewmodel.OnlineStorageViewModel
import dev.zbysiu.homer.ui.viewmodel.PageRecordsDetailViewModel
import dev.zbysiu.homer.ui.viewmodel.SearchViewModel
import dev.zbysiu.homer.ui.viewmodel.StatisticsViewModel
import dev.zbysiu.homer.ui.viewmodel.SuggestionsViewModel
import dev.zbysiu.homer.ui.viewmodel.TimelineViewModel
import dev.zbysiu.homer.ui.viewmodel.UserViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModels[modelClass]?.get() as T
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun mainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookListViewModel::class)
    internal abstract fun bookListViewModel(viewModel: BookListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookDetailViewModel::class)
    internal abstract fun bookDetailViewModel(viewModel: BookDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManualAddViewModel::class)
    internal abstract fun manualAddViewModel(viewModel: ManualAddViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    internal abstract fun statisticsViewModel(viewModel: StatisticsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BackupViewModel::class)
    internal abstract fun backupViewModel(viewModel: BackupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    internal abstract fun searchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeatureFlagConfigViewModel::class)
    internal abstract fun featureFlagConfigViewModel(viewModel: FeatureFlagConfigViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AnnouncementViewModel::class)
    internal abstract fun announcementViewModel(viewModel: AnnouncementViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimelineViewModel::class)
    internal abstract fun timelineViewModel(viewModel: TimelineViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LabelManagementViewModel::class)
    internal abstract fun labelManagementViewModel(viewModel: LabelManagementViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LabelCategoryViewModel::class)
    internal abstract fun labelCategoryViewModel(viewModel: LabelCategoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ImportBooksStorageViewModel::class)
    internal abstract fun importBooksStorageViewModel(viewModel: ImportBooksStorageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnlineStorageViewModel::class)
    internal abstract fun onlineStorageViewModel(viewModel: OnlineStorageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PageRecordsDetailViewModel::class)
    internal abstract fun pageRecordsDetailViewModel(viewModel: PageRecordsDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SuggestionsViewModel::class)
    internal abstract fun suggestionsViewModel(viewModel: SuggestionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MailLoginViewModel::class)
    internal abstract fun mailLoginViewModel(viewModel: MailLoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    internal abstract fun userViewModel(viewModel: UserViewModel): ViewModel
}