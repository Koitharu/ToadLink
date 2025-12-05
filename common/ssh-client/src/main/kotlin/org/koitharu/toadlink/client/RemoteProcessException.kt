package org.koitharu.toadlink.client

class RemoteProcessException(
    val exitCode: Int,
    message: String?,
) : RuntimeException(message) {

    companion object {

        const val EXIT_CODE_NOT_FOUND = 127
    }
}