package org.koitharu.toadlink.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
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
import kotlinx.coroutines.launch
import org.koitharu.toadlink.MainActivity
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.util.checkNotificationPermission
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionService : LifecycleService() {

    @Inject
    lateinit var connectionManager: SshConnectionManager

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_DISCONNECT)
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
        val deviceId = intent?.getIntExtra(ARG_DEVICE_ID, 0) ?: 0
        if (deviceId == 0) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val device = connectionManager.activeConnection.value?.deviceDescriptor
        if (device == null || device.id != deviceId) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val notification = createNotification(device)
        ServiceCompat.startForeground(
            this,
            deviceId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
        lifecycleScope.launch {
            connectionManager.activeConnection.collect { connection ->
                if (connection == null || connection.deviceDescriptor.id != deviceId) {
                    stopSelf(startId)
                } else if (checkNotificationPermission()) {
                    notificationManager.notify(
                        deviceId,
                        createNotification(connection.deviceDescriptor)
                    )
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private fun handleBroadcast(intent: Intent) {
        when (intent.action) {
            ACTION_DISCONNECT -> {
                val deviceId = intent.data?.schemeSpecificPart?.toIntOrNull() ?: return
                if (connectionManager.activeConnection.value?.deviceDescriptor?.id == deviceId) {
                    connectionManager.disconnect()
                }
            }
        }
    }

    private fun createNotification(device: DeviceDescriptor): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setContentTitle(getString(R.string.connected_to_s, device.displayName))
        builder.setSmallIcon(R.drawable.ic_stat_device)
        builder.setSilent(true)
        builder.setPriority(PRIORITY_MIN)
        builder.setContentIntent(
            PendingIntentCompat.getActivity(
                this,
                0,
                MainActivity.IntentBuilder(this).build(),
                0,
                false
            )
        )
        val disconnectIntent = Intent(ACTION_DISCONNECT)
            .setData("conn://${device.id}".toUri())
        val action = NotificationCompat.Action(
            null, getString(R.string.disconnect),
            PendingIntentCompat.getBroadcast(this, 0, disconnectIntent, 0, false)
        )
        builder.addAction(action)
        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_MIN)
        channel.setName(getString(R.string.active_connection))
        channel.setShowBadge(true)
        channel.setLightsEnabled(false)
        channel.setVibrationEnabled(false)
        channel.setSound(null, null)
        notificationManager.createNotificationChannel(channel.build())
    }

    companion object {

        private const val CHANNEL_ID = "connection"
        private const val ARG_DEVICE_ID = "device_id"
        private const val ACTION_DISCONNECT = "org.koitharu.toadlink.ACTION_DISCONNECT"

        fun start(context: Context, deviceId: Int) {
            val intent = Intent(context, ConnectionService::class.java)
            intent.putExtra(ARG_DEVICE_ID, deviceId)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}