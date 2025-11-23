package org.koitharu.toadconnect.client

import kotlinx.coroutines.flow.Flow
import org.koitharu.toadlink.core.DeviceDescriptor

interface SshConnection {

    val deviceDescriptor: DeviceDescriptor

    suspend fun execute(cmdline: String): String

    fun executeContinuously(cmdline: String): Flow<String>

    suspend fun getFileContent(path: String): ByteArray
}