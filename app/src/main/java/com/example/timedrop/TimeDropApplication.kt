package com.example.timedrop

import android.app.Application
import com.example.timedrop.data.monitoring.FirebaseMonitorStore
import com.google.firebase.FirebaseApp

class TimeDropApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        // Initialize monitoring singleton so all SyncRepository instances can write to it
        FirebaseMonitorStore.init(this)
    }

    companion object {
        private var instance: TimeDropApplication? = null
        fun getAppContext(): android.content.Context = instance!!.applicationContext
    }
}
