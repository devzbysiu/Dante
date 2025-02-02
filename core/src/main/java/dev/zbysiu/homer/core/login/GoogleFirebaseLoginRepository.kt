package dev.zbysiu.homer.core.login

import android.annotation.SuppressLint
import android.content.Intent
import dev.zbysiu.homer.core.fromSingleToCompletable
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import dev.zbysiu.homer.util.singleOf
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 *
 * If migrating to firebase, use this docs
 * https://firebase.google.com/docs/auth/android/google-signin
 */
class GoogleFirebaseLoginRepository(
    private val schedulers: SchedulerFacade,
    private val fbAuth: FirebaseAuth
) : LoginRepository {

    private val signInSubject: BehaviorSubject<UserState> = BehaviorSubject.create()

    init {
        postInitialState()
    }

    @SuppressLint("CheckResult")
    private fun postInitialState() {
        getAccount()
            .doOnError(Timber::e)
            .subscribe(signInSubject::onNext) {
                signInSubject.onNext(UserState.Unauthenticated)
            }
    }

    override fun loginWithGoogle(data: Intent): Completable {
        return login(errorMessage = "Cannot sign into Google Account! DanteUser = null") {
            Tasks.await(GoogleSignIn.getSignedInAccountFromIntent(data)).authenticateToFirebase()
        }
    }

    private fun GoogleSignInAccount.authenticateToFirebase(): DanteUser? {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return Tasks.await(fbAuth.signInWithCredential(credential))?.let { authResult ->

            val givenName = authResult.additionalUserInfo?.profile?.get("given_name") as? String
            authResult.user?.toDanteUser(givenName)
        }
    }

    override fun fetchRegisteredAuthenticationSourcesForEmail(mailAddress: String): Single<List<AuthenticationSource>> {
        return singleOf(subscribeOn = schedulers.io) {
            Tasks.await(fbAuth.fetchSignInMethodsForEmail(mailAddress))
                .signInMethods
                ?.map { method ->
                    when (method) {
                        SIGN_UP_METHOD_PASSWORD -> AuthenticationSource.MAIL
                        SIGN_UP_METHOD_GOOGLE -> AuthenticationSource.GOOGLE
                        else -> AuthenticationSource.UNKNOWN
                    }
                }
                ?: listOf()
        }
    }

    override fun createAccountWithMail(mailAddress: String, password: String): Completable {
        return login(errorMessage = "Problem with mail account creation") {
            Tasks.await(fbAuth.createUserWithEmailAndPassword(mailAddress, password)).user?.toDanteUser()
        }
    }

    override fun loginWithMail(mailAddress: String, password: String): Completable {
        return login(errorMessage = "Problem with mail account login") {
            Tasks.await(fbAuth.signInWithEmailAndPassword(mailAddress, password)).user?.toDanteUser()
        }
    }

    override fun updateMailPassword(password: String): Completable {
        return Completable.create { emitter ->

            val currentUser = fbAuth.currentUser
            if (currentUser == null) {
                emitter.tryOnError(NullPointerException("User is not logged in!"))
            } else {
                currentUser.updatePassword(password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emitter.onComplete()
                    } else {
                        val exception = task.exception
                            ?: IllegalStateException("Could not change password due to unknown error")
                        emitter.tryOnError(exception)
                    }
                }
            }
        }
    }

    override fun sendPasswordResetRequest(mailAddress: String): Completable {
        return Completable.create { emitter ->
            fbAuth.sendPasswordResetEmail(mailAddress).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.tryOnError(task.exception ?: IllegalStateException("Could not send password reset request"))
                }
            }
        }
    }

    override fun loginAnonymously(): Completable {
        return login(errorMessage = "Cannot anonymously sign into Firebase! AuthResult = null") {
            Tasks.await(fbAuth.signInAnonymously()).user?.toDanteUser()
        }
    }

    private fun login(errorMessage: String, loginBlock: () -> DanteUser?): Completable {
        return Single
            .fromCallable {
                loginBlock() ?: throw LoginException(errorMessage)
            }
            .doOnSuccess { user ->
                signInSubject.onNext(UserState.SignedInUser(user))
            }
            .fromSingleToCompletable()
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun logout(): Completable {
        return Completable
            .fromAction(fbAuth::signOut)
            .doOnComplete {
                signInSubject.onNext(UserState.Unauthenticated)
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun upgradeAnonymousAccount(mailAddress: String, password: String): Completable {
        return Completable
            .create { emitter ->

                val currentUser = fbAuth.currentUser
                if (currentUser == null) {
                    emitter.tryOnError(NullPointerException("User is not logged in!"))
                } else {
                    val credentials = EmailAuthProvider.getCredential(mailAddress, password)
                    currentUser.linkWithCredential(credentials).addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            emitter.onComplete()
                        } else {
                            emitter.tryOnError(UpgradeException(authResult.exception))
                        }
                    }
                }
            }
            .doOnIO()
            .andThen(reloadUserAfterAnonymousUpgrade())
    }

    private fun Completable.doOnIO(): Completable {
        return this
            .observeOn(schedulers.io)
            .subscribeOn(schedulers.io)
    }

    private fun reloadUserAfterAnonymousUpgrade(): Completable {
        return reloadAccount()
    }

    override fun observeAccount(): Observable<UserState> {
        return signInSubject
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun reloadAccount(): Completable {
        return getCurrentUserState(forceReload = true)
            .doOnSuccess(signInSubject::onNext)
            .fromSingleToCompletable()
    }

    override fun getAccount(): Single<UserState> {
        return getCurrentUserState()
    }

    override fun isLoggedIn(): Boolean {
        return signInSubject.value is UserState.SignedInUser
    }

    private fun getCurrentUserState(forceReload: Boolean = false): Single<UserState> {
        return singleOf(subscribeOn = schedulers.io, observeOn = schedulers.io) {
            fbAuth.currentUser
                ?.apply {
                    if (forceReload) {
                        Tasks.await(reload())
                    }
                }
                ?.toDanteUser()
                ?.let(UserState::SignedInUser)
                ?: UserState.Unauthenticated
        }
    }

    override fun getAuthorizationHeader(): Single<String> {
        return getAccount().map { acc ->
            val authToken = if (acc is UserState.SignedInUser) acc.user.authToken ?: "" else ""
            getAuthorizationHeader(authToken)
        }
    }

    private fun FirebaseUser.toDanteUser(givenName: String? = this.displayName): DanteUser {

        val authenticationSource = when {
            isAnonymous -> AuthenticationSource.ANONYMOUS
            isGoogleUser() -> AuthenticationSource.GOOGLE
            isMailUser() -> AuthenticationSource.MAIL
            else -> AuthenticationSource.UNKNOWN
        }

        return DanteUser(
            givenName,
            this.displayName,
            this.email,
            this.photoUrl,
            Tasks.await(this.getIdToken(false))?.token,
            this.uid,
            authenticationSource
        )
    }

    private fun FirebaseUser.isGoogleUser(): Boolean {
        return providerData.find { it.providerId == "google.com" } != null
    }

    private fun FirebaseUser.isMailUser(): Boolean {
        return providerData.find { it.providerId == "password" } != null
    }

    companion object {

        private const val SIGN_UP_METHOD_PASSWORD = "password"
        private const val SIGN_UP_METHOD_GOOGLE = "google.com"
    }
}