package dev.zbysiu.homer.core.login

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    08.06.2018
 */
interface LoginRepository {

    fun loginWithGoogle(data: Intent): Completable

    fun fetchRegisteredAuthenticationSourcesForEmail(mailAddress: String): Single<List<AuthenticationSource>>

    fun createAccountWithMail(mailAddress: String, password: String): Completable

    fun loginWithMail(mailAddress: String, password: String): Completable

    fun updateMailPassword(password: String): Completable

    fun sendPasswordResetRequest(mailAddress: String): Completable

    fun loginAnonymously(): Completable

    fun logout(): Completable

    fun upgradeAnonymousAccount(mailAddress: String, password: String): Completable

    fun observeAccount(): Observable<UserState>

    fun reloadAccount(): Completable

    fun getAccount(): Single<UserState>

    fun isLoggedIn(): Boolean

    fun getAuthorizationHeader(): Single<String>

    /**
     * Default implementation for creating the bearer header
     */
    fun getAuthorizationHeader(authToken: String): String {
        return "Bearer $authToken"
    }
}