package dev.zbysiu.homer.injection

import android.app.Application
import android.content.SharedPreferences
import dev.zbysiu.homer.backup.BackupRepository
import dev.zbysiu.homer.backup.DefaultBackupRepository
import dev.zbysiu.homer.backup.provider.BackupProvider
import dev.zbysiu.homer.backup.provider.csv.LocalCsvBackupProvider
import dev.zbysiu.homer.backup.provider.external.ExternalStorageBackupProvider
import dev.zbysiu.homer.backup.provider.google.DriveClient
import dev.zbysiu.homer.backup.provider.google.DriveRestClient
import dev.zbysiu.homer.backup.provider.google.GoogleDriveBackupProvider
import dev.zbysiu.homer.backup.provider.shockbytes.ShockbytesHerokuServerBackupProvider
import dev.zbysiu.homer.backup.provider.shockbytes.api.ShockbytesHerokuApi
import dev.zbysiu.homer.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import dev.zbysiu.homer.backup.provider.shockbytes.storage.SharedPreferencesInactiveShockbytesBackupStorage
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.login.GoogleAuth
import dev.zbysiu.homer.importer.DanteCsvImportProvider
import dev.zbysiu.homer.importer.DanteExternalStorageImportProvider
import dev.zbysiu.homer.importer.DefaultImportRepository
import dev.zbysiu.homer.importer.GoodreadsCsvImportProvider
import dev.zbysiu.homer.importer.ImportProvider
import dev.zbysiu.homer.importer.ImportRepository
import dev.zbysiu.homer.core.login.LoginRepository
import dev.zbysiu.homer.storage.DefaultExternalStorageInteractor
import dev.zbysiu.homer.storage.ExternalStorageInteractor
import dev.zbysiu.homer.storage.reader.CsvReader
import dev.zbysiu.homer.util.permission.PermissionManager
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.tracking.Tracker
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

@Module
class BookStorageModule(private val app: Application) {

    @Provides
    fun provideInactiveShockbytesBackupStorage(
        preferences: SharedPreferences
    ): InactiveShockbytesBackupStorage {
        return SharedPreferencesInactiveShockbytesBackupStorage(preferences)
    }

    @Provides
    fun provideBackupRepository(
        backupProvider: Array<BackupProvider>,
        preferences: SharedPreferences,
        tracker: Tracker
    ): BackupRepository {
        return DefaultBackupRepository(backupProvider.toList(), preferences, tracker)
    }

    @Provides
    fun provideExternalStorageInteractor(): ExternalStorageInteractor {
        return DefaultExternalStorageInteractor(app.applicationContext)
    }

    @Provides
    fun provideDriveClient(
        loginRepository: LoginRepository,
        googleAuth: GoogleAuth
    ): DriveClient {
        return DriveRestClient(loginRepository, googleAuth)
    }

    @Provides
    fun provideBackupProvider(
        schedulerFacade: SchedulerFacade,
        loginRepository: LoginRepository,
        shockbytesHerokuApi: ShockbytesHerokuApi,
        inactiveShockbytesBackupStorage: InactiveShockbytesBackupStorage,
        externalStorageInteractor: ExternalStorageInteractor,
        permissionManager: PermissionManager,
        csvImportProvider: DanteCsvImportProvider,
        driveClient: DriveClient
    ): Array<BackupProvider> {
        return arrayOf(
            GoogleDriveBackupProvider(
                schedulerFacade,
                driveClient
            ),
            ShockbytesHerokuServerBackupProvider(
                loginRepository,
                shockbytesHerokuApi,
                inactiveShockbytesBackupStorage
            ),
            ExternalStorageBackupProvider(
                schedulerFacade,
                Gson(),
                externalStorageInteractor,
                permissionManager
            ),
            LocalCsvBackupProvider(
                schedulerFacade,
                externalStorageInteractor,
                permissionManager,
                csvImportProvider
            )
        )
    }

    @Provides
    fun provideDanteCsvImportProvider(schedulers: SchedulerFacade): DanteCsvImportProvider {
        return DanteCsvImportProvider(CsvReader(), schedulers)
    }

    @Provides
    fun provideDanteExternalStorageImportProvider(): DanteExternalStorageImportProvider {
        return DanteExternalStorageImportProvider(gson = Gson())
    }

    @Provides
    fun provideImportProvider(
        schedulers: SchedulerFacade,
        danteCsvImportProvider: DanteCsvImportProvider,
        danteExternalStorageImportProvider: DanteExternalStorageImportProvider
    ): Array<ImportProvider> {
        return arrayOf(
            GoodreadsCsvImportProvider(CsvReader(), schedulers),
            danteCsvImportProvider,
            danteExternalStorageImportProvider
        )
    }

    @Provides
    fun provideImportRepository(
        importProvider: Array<ImportProvider>,
        bookRepository: BookRepository,
        schedulers: SchedulerFacade
    ): ImportRepository {
        return DefaultImportRepository(importProvider, bookRepository, schedulers)
    }
}