package dev.zbysiu.homer.backup.provider.shockbytes

import androidx.fragment.app.FragmentActivity
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.backup.provider.BackupProvider
import dev.zbysiu.homer.backup.provider.shockbytes.api.ShockbytesHerokuApi
import dev.zbysiu.homer.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import dev.zbysiu.homer.core.login.LoginRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    09.05.2019
 *
 * IMPORTANT: Work in progress... Do not put in production!
 */
class ShockbytesHerokuServerBackupProvider(
    private val loginRepository: LoginRepository,
    private val shockbytesHerokuApi: ShockbytesHerokuApi,
    private val inactiveBackupStorage: InactiveShockbytesBackupStorage
) : BackupProvider {

    // Enable it only in debug mode
    override var isEnabled: Boolean = false // Only initialize when ever needed

    override val backupStorageProvider = BackupStorageProvider.SHOCKBYTES_SERVER

    override fun initialize(activity: FragmentActivity?): Completable {
        return Completable.complete()
    }

    override fun backup(backupContent: BackupContent): Completable {
        return loginRepository.getAuthorizationHeader()
            .flatMapCompletable { token ->
                shockbytesHerokuApi.makeBackup(token, backupContent)
                    .flatMapCompletable { entry ->
                        Timber.d(entry.toString())
                        Completable.complete()
                    }
        }
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return loginRepository.getAuthorizationHeader()
            .flatMap { token ->
                shockbytesHerokuApi.listBackups(token)
                    .map { entries ->
                        val entryStates: List<BackupMetadataState> = entries.map { entry ->
                            BackupMetadataState.Active(entry)
                        }
                        entryStates
                    }
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { activeItems ->
                        inactiveBackupStorage.storeInactiveItems(activeItems)
                    }
                    .onErrorReturn { throwable ->
                        Timber.e(throwable)
                        inactiveBackupStorage.getInactiveItems()
                    }
            }
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return loginRepository.getAuthorizationHeader()
            .flatMapCompletable { token ->
                shockbytesHerokuApi.removeBackupById(token, entry.id)
            }
    }

    override fun removeAllBackupEntries(): Completable {
        return loginRepository.getAuthorizationHeader()
            .flatMapCompletable(shockbytesHerokuApi::removeAllBackups)
    }

    override fun mapBackupToBackupContent(entry: BackupMetadata): Single<BackupContent> {
        return loginRepository.getAuthorizationHeader()
            .flatMap { token ->
                shockbytesHerokuApi
                    .getBooksBackupById(token, entry.id)
                    .subscribeOn(Schedulers.io())
            }
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }
}