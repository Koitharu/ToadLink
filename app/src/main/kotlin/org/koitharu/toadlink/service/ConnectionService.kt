package org.koitharu.toadlink.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.session.MediaSession
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN
import androidx.core.app.PendingIntentCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koitharu.toadlink.client.SshConnection
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.mpris.MPRISClient
import org.koitharu.toadlink.mpris.MediaSessionHelper
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.nav.AppIntentFactory
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionService : LifecycleService() {

    @Inject
    lateinit var connectionManager: SshConnectionManager

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var intentFactory: AppIntentFactory

    private lateinit var broadcastReceiver: BroadcastReceiver
    private var mediaListenerJob: Job? = null
    private var connectionsJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                if (intent != null) {
                    handleBroadcast(intent)
                }
            }
        }
        val intentFilter = IntentFilter(ACTION_DISCONNECT)
        intentFilter.addDataScheme("conn")
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    @SuppressLint("InlinedApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val connections = connectionManager.connections.value
        if (connections.isEmpty()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val notification = createServiceNotification(connections.values.first().host)
        ServiceCompat.startForeground(
            this,
            connections.values.first().host.id,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
        connectionsJob?.cancel()
        connectionsJob = observeConnections()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private fun observeConnections() = lifecycleScope.launch(Dispatchers.Default) {
        val onNewConnection = flow {
            val accumulator = HashSet<DeviceDescriptor>()
            connectionManager.connections.collect { items ->
                val connections = items.values.filterNot {
                    it.host in accumulator
                }.forEach {
                    emit(it)
                }
                accumulator.addAll(items.keys)
            }
        }.shareIn(this, SharingStarted.Lazily)
        launch {
            onNewConnection.collect {
                it.launch(Dispatchers.Main) {
                    handleMediaSession(it)
                }
                it.launch {
                    try {
                        notificationManager.notify(
                            it.host.id, createServiceNotification(it.host)
                        )
                        awaitCancellation()
                    } finally {
                        notificationManager.cancel(it.host.id)
                    }
                }
            }
        }
    }

    private fun handleBroadcast(intent: Intent) {
        when (intent.action) {
            ACTION_DISCONNECT -> {
                val deviceId = intent.data?.schemeSpecificPart?.toIntOrNull() ?: return
                lifecycleScope.launch(Dispatchers.Default) {
                    connectionManager.disconnect(deviceId)
                }
            }
        }
    }

    @RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun handleMediaSession(connection: SshConnection) {
        val mprisClient = MPRISClient(connection)
        val notificationId = connection.host.id
        MediaSessionHelper(this, mprisClient).use { mediaSessionHelper ->
            try {
                combine(
                    mprisClient.observeMetadata(),
                    mprisClient.observeState(),
                    ::Pair,
                ).flowOn(Dispatchers.Default)
                    .collectLatest { (metadata, state) ->
                        if (metadata != null) {
                            val mediaSession = mediaSessionHelper.getMediaSession(metadata, state)
                            val notification = createMediaNotification(mediaSession)
                            notificationManager.notify(TAG_MEDIA, notificationId, notification)
                        } else {
                            notificationManager.cancel(TAG_MEDIA, notificationId)
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO
            } finally {
                notificationManager.cancel(TAG_MEDIA, notificationId)
            }
        }
    }

    private fun createServiceNotification(device: DeviceDescriptor): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_MAIN)
        builder.setContentTitle(getString(R.string.connected_to_s, device.displayName))
        builder.setSmallIcon(R.drawable.ic_stat_device)
        builder.setSilent(true)
        builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        builder.setPriority(PRIORITY_MIN)
        builder.setContentIntent(
            PendingIntentCompat.getActivity(
                this,
                0,
                intentFactory.controlIntent(this, device.id),
                0,
                false
            )
        )
        val disconnectIntent = Intent(ACTION_DISCONNECT)
            .setData("conn:${device.id}".toUri())
        val action = NotificationCompat.Action(
            null, getString(R.string.disconnect),
            PendingIntentCompat.getBroadcast(
                this,
                0,
                disconnectIntent,
                PendingIntent.FLAG_CANCEL_CURRENT,
                false
            )
        )
        builder.addAction(action)
        return builder.build()
    }

    private fun createMediaNotification(mediaSession: MediaSession): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID_MEDIA)
        } else {
            Notification.Builder(this)
        }
        val style = Notification.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
        return builder.setStyle(style)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(org.koitharu.toadlink.mpris.R.drawable.ic_stat_player)
            .build()
    }

    private fun createNotificationChannels() {
        val mainChannel = NotificationChannelCompat.Builder(CHANNEL_ID_MAIN, IMPORTANCE_MIN)
        mainChannel.setName(getString(R.string.active_connection))
        mainChannel.setShowBadge(true)
        mainChannel.setLightsEnabled(false)
        mainChannel.setVibrationEnabled(false)
        mainChannel.setSound(null, null)
        notificationManager.createNotificationChannel(mainChannel.build())
        val mediaChannel = NotificationChannelCompat.Builder(
            CHANNEL_ID_MEDIA,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
        mediaChannel.setName(getString(R.string.media))
        mediaChannel.setShowBadge(true)
        mediaChannel.setLightsEnabled(false)
        mediaChannel.setVibrationEnabled(false)
        mediaChannel.setSound(null, null)
        notificationManager.createNotificationChannel(mediaChannel.build())
    }

    companion object {

        private const val CHANNEL_ID_MAIN = "connection"
        private const val CHANNEL_ID_MEDIA = "mpris"
        private const val ACTION_DISCONNECT = "org.koitharu.toadlink.ACTION_DISCONNECT"
        private const val TAG_MEDIA = "mpris_media"

        fun start(context: Context) {
            val intent = Intent(context, ConnectionService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ConnectionService::class.java)
            context.stopService(intent)
        }
    }
}