package org.koitharu.toadlink.client

import kotlinx.coroutines.flow.Flow
import okio.FileSystem
import org.koitharu.toadlink.core.DeviceDescriptor

interface SshConnection {

    val host: DeviceDescriptor

    val fileSystem: FileSystem

    suspend fun execute(cmdline: String): String

    fun executeContinuously(cmdline: String): Flow<String>
}