package com.verywords.csms_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App
            private set

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this.applicationContext
        instance = this
    }
}