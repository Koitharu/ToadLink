package org.koitharu.toadlink.client

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import okio.Path
import okio.buffer
import okio.sink
import java.io.File
import java.io.OutputStream

suspend fun SshConnection.tryExecute(cmdline: String) = runCatching {
    execute(cmdline)
}

fun SshConnection.executeBlocking(cmdline: String) = runBlocking {
    execute(cmdline)
}

suspend fun SshConnection.getOSName() = tryExecute("uname -o").getOrNull()

suspend fun SshConnection.getCmdCompletion(cmdline: String): ImmutableList<String>? {
    if (cmdline.startsWith('-')) {
        return null
    }
    val res = tryExecute("compgen -abcdefk $cmdline").getOrNull() ?: return null
    return res.lines().distinct().toImmutableList()
}

suspend fun SshConnection.scp(source: Path, target: File) = runInterruptible(Dispatchers.IO) {
    target.sink().buffer().use { output ->
        fileSystem.source(source).use { input ->
            output.writeAll(input)
        }
    }
}

suspend fun SshConnection.scp(source: Path, target: OutputStream) = runInterruptible(Dispatchers.IO) {
    target.sink().buffer().use { output ->
        fileSystem.source(source).use { input ->
            output.writeAll(input)
        }
    }
}