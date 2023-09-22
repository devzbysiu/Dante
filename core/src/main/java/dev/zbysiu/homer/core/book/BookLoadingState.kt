package dev.zbysiu.homer.core.book

import androidx.annotation.StringRes

sealed class BookLoadingState {

    object Loading : BookLoadingState()

    data class Error(@StringRes val cause: Int) : BookLoadingState()

    data class Success(val bookSuggestion: BookSuggestion) : BookLoadingState()
}