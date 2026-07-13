package org.koitharu.toadlink.client

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import okio.Path
import okio.buffer
import okio.sink
import org.connectbot.sshlib.AuthResult
import org.connectbot.sshlib.SshClient
import org.connectbot.sshlib.SshClientConfig
import org.connectbot.sshlib.SshException
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.util.runCatchingCancellable
import java.io.File
import java.io.OutputStream

public suspend fun SshConnection.tryExecute(cmdline: String): Result<String> =
    runCatchingCancellable {
        execute(cmdline)
    }

internal fun SshConnection.executeBlocking(cmdline: String) = runBlocking {
    execute(cmdline)
}

public suspend fun SshConnection.getOSName(): String? = tryExecute("uname -o").getOrNull()

public suspend fun SshConnection.getCmdCompletion(cmdline: String): ImmutableList<String>? {
    if (cmdline.startsWith('-')) {
        return null
    }
    val res = tryExecute("compgen -abcdefk $cmdline").getOrNull() ?: return null
    return res.lines().distinct().toImmutableList()
}

public suspend fun SshConnection.scp(source: Path, target: File): Long =
    runInterruptible(Dispatchers.IO) {
        target.sink().buffer().use { output ->
            fileSystem.source(source).use { input ->
                output.writeAll(input)
            }
        }
    }

public suspend fun SshConnection.scp(source: Path, target: OutputStream): Long =
    runInterruptible(Dispatchers.IO) {
        target.sink().buffer().use { output ->
            fileSystem.source(source).use { input ->
                output.writeAll(input)
            }
        }
    }

internal fun DeviceDescriptor.createClient(): SshClient {
    val config = SshClientConfig {
        host = this@createClient.hostname
        port = this@createClient.port
        preferPasswordAuth = this@createClient.key == null
        hostKeyVerifier = AppHostKeyVerifier()
    }
    return SshClient(config)
}

internal suspend fun SshClient.authenticate(descriptor: DeviceDescriptor): AuthResult {
    return descriptor.key?.let { key ->
        authenticatePublicKey(
            username = descriptor.username,
            privateKeyData = key,
            passphrase = descriptor.password
        )
    } ?: authenticatePassword(
        username = descriptor.username,
        password = descriptor.password
    )
}

internal fun AuthResult.ensureSuccess() = when (this) {
    is AuthResult.Error -> throw SshException(message, cause)
    is AuthResult.Failure -> throw SshException("Auth method is not supported: [${allowedMethods.joinToString()}]")
    AuthResult.Success -> Unit
}