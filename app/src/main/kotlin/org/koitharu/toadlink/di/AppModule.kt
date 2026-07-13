package org.koitharu.toadlink.di

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import org.koitharu.toadlink.MainActivity
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.network.NetworkScanner
import org.koitharu.toadlink.ui.nav.AppIntentFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideProcessLifecycleScope(): LifecycleCoroutineScope =
        ProcessLifecycleOwner.get().lifecycle.coroutineScope

    @Provides
    @Singleton
    fun provideSshManager(
        processLifecycleCoroutineScope: LifecycleCoroutineScope
    ) = SshConnectionManager(
        coroutineScope = processLifecycleCoroutineScope + Dispatchers.Default
    )

    @Provides
    @Singleton
    fun provideNetworkScanner(
        wifiManager: WifiManager,
    ) = NetworkScanner(
        wifiManager = wifiManager,
    )

    @Provides
    fun provideAppIntentFactory(): AppIntentFactory = MainActivity

    @Provides
    fun provideImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader = SingletonImageLoader.get(context)
}