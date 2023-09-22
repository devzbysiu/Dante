package dev.zbysiu.homer.backup.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.zbysiu.homer.R
import dev.zbysiu.homer.util.Priority
import dev.zbysiu.homer.util.Stability
import com.google.gson.annotations.SerializedName

enum class BackupStorageProvider(
    val acronym: String,
    val title: String,
    @DrawableRes val icon: Int,
    @StringRes val rationale: Int,
    val priority: Priority,
    val isLocalFileExportable: Boolean,
    val stability: Stability
) {

    @SerializedName("na")
    UNKNOWN(
        "na",
        "na",
        R.drawable.ic_unknown,
        R.string.na,
        Priority.LOW,
        isLocalFileExportable = false,
        stability = Stability.RELEASE
    ),
    @SerializedName("shock_server")
    SHOCKBYTES_SERVER(
        "shock_server",
        "Shockbytes Server",
        R.drawable.ic_shockbytes,
        R.string.backup_storage_provider_rationale_shockbytes,
        Priority.HIGH,
        isLocalFileExportable = false,
        stability = Stability.CANARY
    ),
    @SerializedName("google-drive")
    GOOGLE_DRIVE(
        "google-drive",
        "Google Drive",
        R.drawable.ic_google_drive,
        R.string.backup_storage_provider_rationale_gdrive,
        Priority.HIGH,
        isLocalFileExportable = false,
        stability = Stability.RELEASE
    ),
    @SerializedName("ext_storage")
    EXTERNAL_STORAGE(
        "ext-storage",
        "External Storage",
        R.drawable.ic_external_storage,
        R.string.backup_storage_provider_rationale_external_storage,
        Priority.MEDIUM,
        isLocalFileExportable = false,
        stability = Stability.RELEASE
    ),
    @SerializedName("csv_local")
    LOCAL_CSV(
        "csv-local",
        "CSV Export",
        R.drawable.ic_csv,
        R.string.backup_storage_provider_rationale_csv,
        Priority.MEDIUM,
        isLocalFileExportable = false,
        stability = Stability.BETA
    );

    companion object {

        fun byAcronym(acronym: String): BackupStorageProvider {
            return values().find { it.acronym == acronym } ?: UNKNOWN
        }
    }
}