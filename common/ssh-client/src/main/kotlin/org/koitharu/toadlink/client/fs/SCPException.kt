package org.koitharu.toadlink.client.fs

import okio.IOException

class SCPException internal constructor(
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)