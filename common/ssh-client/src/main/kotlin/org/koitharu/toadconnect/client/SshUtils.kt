package org.koitharu.toadconnect.client

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

suspend fun SshConnection.tryExecute(cmdline: String) = runCatching {
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