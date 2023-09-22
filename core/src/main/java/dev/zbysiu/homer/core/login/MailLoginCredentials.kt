package dev.zbysiu.homer.core.login

data class MailLoginCredentials(
    val address: String,
    val password: String,
    val isSignUp: Boolean
)