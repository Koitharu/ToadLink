package org.koitharu.toadlink.client.exceptions

public class RemoteProcessException internal constructor(
    public val exitCode: Int,
    message: String?,
) : SshIOException(message, null) {

    public companion object {

        public const val EXIT_CODE_NOT_FOUND: Int = 127
    }
}