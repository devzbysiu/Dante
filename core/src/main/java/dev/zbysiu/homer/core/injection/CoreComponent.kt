package dev.zbysiu.homer.core.injection

import dev.zbysiu.homer.core.book.realm.RealmInstanceProvider
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.core.data.PageRecordDao
import dev.zbysiu.homer.core.data.ReadingGoalRepository
import dev.zbysiu.homer.core.image.ImageLoader
import dev.zbysiu.homer.core.image.picker.ImagePicking
import dev.zbysiu.homer.core.login.GoogleAuth
import dev.zbysiu.homer.core.login.LoginRepository
import dev.zbysiu.homer.core.network.BookDownloader
import dev.zbysiu.homer.core.network.DetailsDownloader
import dev.zbysiu.homer.core.network.google.BookDetailsApi
import dev.zbysiu.homer.core.network.google.GoogleBooksApi
import dev.zbysiu.homer.core.user.UserRepository
import dev.zbysiu.homer.util.scheduler.SchedulerFacade
import com.google.firebase.auth.FirebaseAuth
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Component(
    modules = [
        CoreModule::class,
        NetworkModule::class,
        LoginModule::class
    ]
)
@Singleton
interface CoreComponent {

    fun getBookRepository(): BookRepository
    fun getPageRecordDao(): PageRecordDao
    fun getBookDownloader(): BookDownloader
    fun getRealmInstanceProvider(): RealmInstanceProvider
    fun getReadingGoalRepository(): ReadingGoalRepository

    fun getGoogleAuth(): GoogleAuth
    fun getLoginRepository(): LoginRepository
    fun getUserRepository(): UserRepository
    fun getFirebaseAuth(): FirebaseAuth

    fun getImageLoader(): ImageLoader
    fun getImagePicker(): ImagePicking
    fun getSchedulerFacade(): SchedulerFacade

    fun getOkHttpClient(): OkHttpClient
    fun provideGoogleBooksApi(): GoogleBooksApi

    fun getBookDetailsDownloader(): DetailsDownloader
    fun provideBookDetailsApi(): BookDetailsApi
}
