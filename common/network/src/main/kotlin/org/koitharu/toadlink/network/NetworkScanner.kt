package org.koitharu.toadlink.network

import android.net.wifi.WifiManager
import android.text.format.Formatter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import org.koitharu.toadlink.core.IpAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

class NetworkScanner(
    private val wifiManager: WifiManager,
) {

    fun observeLocalNetwork(): Flow<ImmutableList<String>> = channelFlow<IpAddress> {
        val localIp = IpAddress.parse(getLocalIpAddress() ?: return@channelFlow) as IpAddress.IPv4
        val (net, subnet) = localIp
        if (net != 192.toUByte() || subnet != 168.toUByte()) {
            return@channelFlow
        }
        for (i in 1 until 255) {
            val ip = localIp.copy(b4 = i.toUByte())
            launch {
                if (probePort(ip.toString(), 22)) {
                    send(ip)
                }
            }
        }
    }.runningFold(persistentListOf()) { list, ipAddress ->
        list.add(ipAddress.toString())
    }

    private fun getLocalIpAddress(): String? {
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.getIpAddress()
        return Formatter.formatIpAddress(ipAddress)
    }

    private suspend fun probePort(ip: String, port: Int) = runInterruptible(Dispatchers.IO) {
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), TimeUnit.SECONDS.toMillis(4).toInt())
            }
        }.isSuccess
    }
}