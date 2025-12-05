package org.koitharu.toadlink.files.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class LocalFileCache @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val root = File(
        context.externalCacheDir ?: context.cacheDir,
        "remote"
    )

    fun createTempFile(originalName: String): File {
        if (!root.exists()) {
            root.mkdir()
        }
        val file = File(root, originalName)
        file.createNewFile()
        return file
    }

    fun getFileUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        LocalFileProvider.AUTHORITY,
        file
    )
}