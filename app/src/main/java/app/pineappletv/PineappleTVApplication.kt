package app.pineappletv

import android.app.Application
import app.pineappletv.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PineappleTVApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@PineappleTVApplication)
            modules(appModule)
        }
    }
}