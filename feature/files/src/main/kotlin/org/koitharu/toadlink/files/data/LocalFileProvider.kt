package org.koitharu.toadlink.files.data

import androidx.core.content.FileProvider

class LocalFileProvider : FileProvider() {

    companion object {

        const val AUTHORITY = "org.koitharu.toadlink.files"
    }
}