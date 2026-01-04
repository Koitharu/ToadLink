package org.koitharu.toadlink.client

import kotlinx.coroutines.flow.Flow
import okio.FileSystem
import okio.Source
import org.koitharu.toadlink.core.DeviceDescriptor

interface SshConnection {

    val host: DeviceDescriptor

    val fileSystem: FileSystem

    suspend fun execute(cmdline: String): String

    suspend fun executeAsSource(cmdline: String): Source

    fun executeContinuously(cmdline: String): Flow<String>
}