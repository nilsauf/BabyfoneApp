package de.nilsauf.babyfone.hilt

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemServicesModule {

    @Singleton
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService()!!
    }

    @Singleton
    @Provides
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager {
        return context.getSystemService()!!
    }

    @Singleton
    @Provides
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return context.getSystemService()!!
    }
}