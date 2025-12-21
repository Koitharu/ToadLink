package org.koitharu.toadlink.di

import android.content.Context
import android.net.wifi.WifiManager
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AndroidModule {

    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context,
    ): NotificationManagerCompat = NotificationManagerCompat.from(context)

    @Provides
    fun provideWifiManager(
        @ApplicationContext context: Context,
    ) = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
}