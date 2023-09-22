package dev.zbysiu.homer.core.login

sealed class UserState {

    data class SignedInUser(val user: DanteUser) : UserState()

    object Unauthenticated : UserState()
}
