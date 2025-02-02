package dev.zbysiu.homer.core.injection

import android.content.Context
import dev.zbysiu.homer.core.R
import dev.zbysiu.homer.core.login.GoogleAuth
import dev.zbysiu.homer.core.login.GoogleFirebaseLoginRepository
import dev.zbysiu.homer.core.login.LoginRepository
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LoginModule(private val context: Context) {

    private fun provideGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.oauth_client_id))
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
            .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    @Provides
    @Singleton
    fun provideLoginRepository(
        schedulers: SchedulerFacade,
        fbAuth: FirebaseAuth
    ): LoginRepository {
        return GoogleFirebaseLoginRepository(schedulers, fbAuth)
    }

    @Provides
    @Singleton
    fun provideGoogleAuth(): GoogleAuth {
        return GoogleAuth(provideGoogleSignInClient(), context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}