package org.koitharu.toadlink.client

import kotlinx.coroutines.flow.Flow
import okio.BufferedSink
import okio.BufferedSource
import org.koitharu.toadlink.core.DeviceDescriptor

interface SshConnection {

    val deviceDescriptor: DeviceDescriptor

    suspend fun execute(cmdline: String): String

    fun executeContinuously(cmdline: String): Flow<String>

    suspend fun getFileContent(path: String): BufferedSource

    suspend fun getFileContent(path: String, target: BufferedSink)
}