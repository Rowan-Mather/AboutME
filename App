package com.example.aboutme

import android.app.Application

val prefs: DataHandler by lazy {
    App.prefs!!
}

class App: Application()
{
    companion object {
        var prefs: DataHandler? = null
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        prefs = DataHandler(applicationContext)
    }
}
