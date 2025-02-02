package dev.zbysiu.homer.storage

import android.net.Uri
import io.reactivex.Single

interface ImageUploadStorage {

    fun uploadCustomImage(image: Uri, progressListener: ((Int) -> Unit)? = null): Single<Uri>

    fun uploadUserImage(image: Uri): Single<Uri>
}