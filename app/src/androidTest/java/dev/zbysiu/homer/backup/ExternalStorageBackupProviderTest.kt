package dev.zbysiu.homer.backup

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dev.zbysiu.homer.backup.model.BackupContent
import dev.zbysiu.homer.backup.model.BackupServiceConnectionException
import dev.zbysiu.homer.backup.provider.external.ExternalStorageBackupProvider
import dev.zbysiu.homer.storage.ExternalStorageInteractor
import dev.zbysiu.homer.util.scheduler.TestSchedulerFacade
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import dev.zbysiu.homer.ui.activity.MainActivity
import dev.zbysiu.homer.backup.model.BackupItem
import dev.zbysiu.homer.backup.model.BackupMetadata
import dev.zbysiu.homer.backup.model.BackupMetadataState
import dev.zbysiu.homer.backup.model.BackupStorageProvider
import dev.zbysiu.homer.core.book.BookEntity
import dev.zbysiu.homer.util.permission.TestPermissionManager
import dev.zbysiu.test.ObjectCreator
import dev.zbysiu.test.TestResourceManager
import dev.zbysiu.test.any
import io.reactivex.Single
import org.mockito.Mockito.`when`
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class ExternalStorageBackupProviderTest {

    private lateinit var backupProvider: ExternalStorageBackupProvider

    private val externalStorageInteractor = mock(ExternalStorageInteractor::class.java)

    private val activityScenario: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

    @Before
    fun setup() {
        backupProvider = ExternalStorageBackupProvider(
            dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.schedulerFacade,
            dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson,
            externalStorageInteractor,
            dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.permissionManager
        )
    }

    @Test
    fun test_initialize_without_context() {

        backupProvider
            .initialize(null)
            .test()
            .assertError(BackupServiceConnectionException::class.java)

        assertThat(backupProvider.isEnabled).isFalse()
    }

    @Test
    fun test_initialize_without_creating_base_dir() {

        `when`(externalStorageInteractor.createBaseDirectory("Dante"))
            .thenThrow(IllegalStateException::class.java)

        activityScenario.onActivity { activity ->
            backupProvider
                .initialize(activity)
                .test()
                .assertError(IllegalStateException::class.java)

            assertThat(backupProvider.isEnabled).isFalse()
        }
    }

    @Test
    fun test_initialize_working() {

        activityScenario.onActivity { activity ->
            backupProvider
                .initialize(activity)
                .test()
                .assertComplete()

            assertThat(backupProvider.isEnabled).isTrue()
        }
    }

    @Test
    fun test_backup_with_empty_list() {

        val books = listOf<BookEntity>()

        backupProvider.backup(BackupContent(books))
            .test()
            .assertComplete()
    }

    @Test
    fun test_backup_with_populated_list() {

        val books = ObjectCreator.getPopulatedListOfBookEntities()

        backupProvider.backup(BackupContent(books))
            .test()
            .assertComplete()
    }

    @Test
    fun test_backup_with_external_storage_error() {

        `when`(externalStorageInteractor.writeToFileInDirectory(any(), any(), any()))
            .thenThrow(IllegalStateException::class.java)

        val books = ObjectCreator.getPopulatedListOfBookEntities()

        backupProvider.backup(BackupContent(books))
            .test()
            .assertNotComplete()
            .assertError(IllegalStateException::class.java)
    }

    @Test
    fun test_getBackupEntries() {

        val file1 = File("entry_1.dbi")
        val file2 = File("entry_2.dbi")

        val metadata = dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.fromJson<List<BackupMetadata.Standard>>(TestResourceManager.getTestResourceAsString(javaClass, "/backup_entries.json"))
        val expected = metadata.map { BackupMetadataState.Active(it) }

        val backupItem1 = BackupItem(metadata[0], ObjectCreator.getPopulatedListOfBookEntities())
        val backupItem2 = BackupItem(metadata[1], ObjectCreator.getPopulatedListOfBookEntities().subList(0, 1))

        `when`(externalStorageInteractor.listFilesInDirectory(any(), any()))
            .thenReturn(Single.just(listOf(file1, file2)))

        `when`(externalStorageInteractor.readFileContent("Dante", file1.name))
            .thenReturn(dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.toJson(backupItem1))

        `when`(externalStorageInteractor.readFileContent("Dante", file2.name))
            .thenReturn(dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.toJson(backupItem2))

        backupProvider.getBackupEntries()
            .test()
            .assertValue { states ->
                states == expected
            }
    }

    @Test
    fun test_getBackupEntries_no_entries() {

        `when`(externalStorageInteractor.listFilesInDirectory(any(), any()))
            .thenReturn(Single.just(listOf()))

        backupProvider.getBackupEntries()
            .test()
            .assertValue { states -> states == listOf<File>() }
            .assertComplete()
    }

    @Test
    fun test_getBackupEntries_backup_file_corrupted() {

        val file1 = File("entry_1.dbi")
        val file2 = File("entry_2.dbi")

        val metadata = dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.fromJson<List<BackupMetadata.Standard>>(TestResourceManager.getTestResourceAsString(javaClass, "/backup_entries.json"))
        val expected = listOf(metadata.map { BackupMetadataState.Active(it) }.first())

        val backupItem1 = BackupItem(metadata[0], ObjectCreator.getPopulatedListOfBookEntities())
        val backupItem2 = BackupItem(metadata[1], ObjectCreator.getPopulatedListOfBookEntities().subList(0, 1))

        val corruptJson2 = dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.toJson(backupItem2).lineSequence().drop(1).joinToString("")

        `when`(externalStorageInteractor.listFilesInDirectory(any(), any()))
            .thenReturn(Single.just(listOf(file1, file2)))

        `when`(externalStorageInteractor.readFileContent("Dante", file1.name))
            .thenReturn(dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.toJson(backupItem1))

        `when`(externalStorageInteractor.readFileContent("Dante", file2.name))
            .thenReturn(corruptJson2)

        backupProvider.getBackupEntries()
            .test()
            .assertValue { states ->
                states == expected
            }
    }

    @Test
    fun test_mapEntryToBooks() {

        val metadata = BackupMetadata.Standard(
            id = "12345",
            device = "Nokia 7.1",
            storageProvider = BackupStorageProvider.EXTERNAL_STORAGE,
            books = 3,
            timestamp = System.currentTimeMillis(),
            fileName = "test_mapEntryToBooks.json"
        )

        val expected = ObjectCreator.getPopulatedListOfBookEntities()
        val backupItem = BackupItem(metadata, expected)

        `when`(externalStorageInteractor.readFileContent("Dante", metadata.fileName))
            .thenReturn(dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.toJson(backupItem))

        backupProvider.mapBackupToBackupContent(metadata)
            .test()
            .assertValue(BackupContent(expected))
    }

    @Test
    fun test_mapEntryToBooks_corrupt_json() {

        val metadata = BackupMetadata.Standard(
            id = "12345",
            device = "Nokia 7.1",
            storageProvider = BackupStorageProvider.EXTERNAL_STORAGE,
            books = 3,
            timestamp = System.currentTimeMillis(),
            fileName = "test_mapEntryToBooks.json"
        )

        val expected = ObjectCreator.getPopulatedListOfBookEntities()
        val backupItem = BackupItem(metadata, expected)
        val corruptJson = dev.zbysiu.homer.backup.ExternalStorageBackupProviderTest.Companion.gson.toJson(backupItem).lineSequence().drop(1).joinToString()

        `when`(externalStorageInteractor.readFileContent("Dante", metadata.fileName))
            .thenReturn(corruptJson)

        backupProvider.mapBackupToBackupContent(metadata)
            .test()
            .assertError(NullPointerException::class.java)
    }

    companion object {

        private val schedulerFacade = TestSchedulerFacade()
        private val gson = Gson()
        private val permissionManager = TestPermissionManager()
    }
}