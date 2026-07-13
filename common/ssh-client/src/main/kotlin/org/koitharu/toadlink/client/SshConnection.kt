package org.koitharu.toadlink.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import okio.FileSystem
import okio.Source
import org.koitharu.toadlink.core.DeviceDescriptor

public interface SshConnection : CoroutineScope {

    public val host: DeviceDescriptor

    public val fileSystem: FileSystem

    public suspend fun execute(cmdline: String): String

    public suspend fun executeAsSource(cmdline: String): Source

    public fun executeContinuously(cmdline: String): Flow<String>
}