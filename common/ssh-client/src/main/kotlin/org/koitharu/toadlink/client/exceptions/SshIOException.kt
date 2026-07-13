package org.koitharu.toadlink.client.exceptions

import okio.IOException

public abstract class SshIOException internal constructor(
    message: String?,
    cause: Throwable?,
) : IOException(message, cause)