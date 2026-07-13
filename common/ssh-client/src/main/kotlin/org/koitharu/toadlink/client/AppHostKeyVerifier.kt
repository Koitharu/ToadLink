package org.koitharu.toadlink.client

import org.connectbot.sshlib.HostKeyVerifier
import org.connectbot.sshlib.PublicKey

internal class AppHostKeyVerifier : HostKeyVerifier {

    override suspend fun verify(key: PublicKey): Boolean {
        return true // TODO
    }
}