package org.koitharu.toadlink.files

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import okio.Path
import okio.Path.Companion.toPath
import org.koitharu.toadlink.files.fs.SshFile

internal data class FileManagerState(
    val path: Path,
    val files: ImmutableList<SshFile>,
    val isLoading: Boolean,
    val loadingFile: String?,
) {

    constructor() : this(
        path = "~".toPath(),
        files = persistentListOf(),
        isLoading = true,
        loadingFile = null,
    )
}