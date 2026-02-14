package org.koitharu.toadlink.files

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import okio.Path
import okio.Path.Companion.toPath
import org.koitharu.toadlink.files.fs.SshFile

@Immutable
internal data class FileManagerState(
    val path: Path,
    val files: ImmutableList<SshFile>,
    val isLoading: Boolean,
    val loadingFile: String?,
    val showThumbnails: Boolean,
    val gridView: Boolean,
) {

    constructor() : this(
        path = "~".toPath(),
        files = persistentListOf(),
        isLoading = true,
        loadingFile = null,
        showThumbnails = false,
        gridView = false,
    )
}