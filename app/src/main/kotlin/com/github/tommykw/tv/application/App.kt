package com.github.tommykw.tv.application

import android.app.Application
import com.github.tommykw.tv.di.AppComponent
import javax.inject.Inject

class App : Application() {
    companion object {
        @JvmStatic
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
    }
}