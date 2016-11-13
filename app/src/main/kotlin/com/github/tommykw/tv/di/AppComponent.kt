package com.github.tommykw.tv.di

import com.github.tommykw.tv.activity.MainActivity
import com.github.tommykw.tv.application.App
import com.github.tommykw.tv.fragment.MainFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(application: App)
    fun inject(activity: MainActivity)
    fun inject(fragment: MainFragment)
}