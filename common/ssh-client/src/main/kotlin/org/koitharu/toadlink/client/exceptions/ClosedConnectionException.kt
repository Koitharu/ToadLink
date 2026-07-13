package org.koitharu.toadlink.client.exceptions

public class ClosedConnectionException internal constructor(
    cause: Throwable? = null
) : SshIOException("Connection was closed", cause)