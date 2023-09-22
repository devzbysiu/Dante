package dev.zbysiu.homer.util

import timber.log.Timber

object ExceptionHandlers {

    fun defaultExceptionHandler(throwable: Throwable) = Timber.e(throwable)
}