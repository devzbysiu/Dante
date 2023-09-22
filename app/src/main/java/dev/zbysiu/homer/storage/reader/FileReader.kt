package dev.zbysiu.homer.storage.reader

import io.reactivex.Single
import java.io.File

interface FileReader {

    fun readFile(file: File): Sequence<String>

    fun readWholeFile(file: File): Single<String>
}