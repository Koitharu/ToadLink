package org.koitharu.toadlink

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.service.ConnectionService
import org.koitharu.toadlink.utils.coil.SshImageFetcher
import javax.inject.Inject

@HiltAndroidApp
class ToadApp : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var connectionManager: SshConnectionManager

    override fun onCreate() {
        super.onCreate()
        SshConnectionManager.setLoggingEnabled(true) // TODO false for release
        observeConnection()
    }

    override fun newImageLoader(
        context: PlatformContext,
    ): ImageLoader = ImageLoader.Builder(applicationContext)
        .components {
            add(SshImageFetcher.Factory(connectionManager))
        }.build()

    private fun observeConnection() {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.coroutineScope.launch {
            connectionManager.activeConnection
                .flowWithLifecycle(lifecycle)
                .collect { connection ->
                    if (connection != null) {
                        ConnectionService.start(this@ToadApp, connection.deviceDescriptor.id)
                    }
                }
        }
    }
}