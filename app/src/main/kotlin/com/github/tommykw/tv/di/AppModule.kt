package com.github.tommykw.tv.di

import com.github.tommykw.tv.application.App
import com.github.tommykw.tv.di.scope.AppScope
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule(val app: App) {
    @Provides
    @Singleton
    @AppScope
    fun provideAppContext() = app

    @Provides
    @Singleton
    @Named("something")
    fun provideSomething() = "something"
}