package org.koitharu.toadlink

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import coil3.util.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.service.ConnectionService
import org.koitharu.toadlink.utils.coil.SshImageFetcher
import org.koitharu.toadlink.utils.coil.thumbnails.SshThumbnailFetcher
import javax.inject.Inject

@HiltAndroidApp
class ToadApp : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var connectionManager: SshConnectionManager

    override fun onCreate() {
        super.onCreate()
        observeConnection()
    }

    override fun newImageLoader(
        context: PlatformContext,
    ): ImageLoader = ImageLoader.Builder(applicationContext)
        .crossfade(true)
        .logger(if (BuildConfig.DEBUG) DebugLogger(Logger.Level.Info) else null)
        .components {
            add(SshThumbnailFetcher.Factory(connectionManager))
            add(SshImageFetcher.Factory(connectionManager))
        }.build()

    private fun observeConnection() {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.coroutineScope.launch {
            connectionManager.connections
                .map { it.isNotEmpty() }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { hasConnections ->
                    if (hasConnections) {
                        ConnectionService.start(this@ToadApp)
                    } else {
                        ConnectionService.stop(this@ToadApp)
                    }
                }
        }
    }
}