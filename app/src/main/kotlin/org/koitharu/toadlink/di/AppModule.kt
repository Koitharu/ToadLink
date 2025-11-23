package org.koitharu.toadlink.di

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import org.koitharu.toadconnect.client.SshConnectionManager
import org.koitharu.toadlink.network.NetworkScanner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSshConnectionManager() = SshConnectionManager(
        coroutineScope = ProcessLifecycleOwner.get().lifecycle.coroutineScope + Dispatchers.Default
    )

    @Provides
    @Singleton
    fun provideNetworkScanner(
        @ApplicationContext context: Context,
    ) = NetworkScanner(
        context = context
    )
}