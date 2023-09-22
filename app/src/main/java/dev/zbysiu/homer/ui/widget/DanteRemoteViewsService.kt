package dev.zbysiu.homer.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import dev.zbysiu.homer.DanteApp
import dev.zbysiu.homer.core.data.BookRepository
import dev.zbysiu.homer.util.settings.DanteSettings
import javax.inject.Inject

class DanteRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var danteSettings: DanteSettings

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DanteApp).appComponent.inject(this)
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return DanteRemoteViewsFactory(this, bookRepository, danteSettings)
    }
}