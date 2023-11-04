package com.example.aboutme

import android.app.Application

//this creates the global variable prefs of the Data Handler class
//which allows all the activities to save date
//this is a standard class for this type of data storage
//I copied most of it from
// https://blog.teamtreehouse.com/making-sharedpreferences-easy-with-kotlin
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