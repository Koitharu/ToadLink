package org.koitharu.toadlink.files

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koitharu.toadlink.core.fs.SshFile
import org.koitharu.toadlink.core.fs.SshPath

internal data class FileManagerState(
    val path: SshPath,
    val files: ImmutableList<SshFile>,
    val isLoading: Boolean,
    val loadingFile: String?,
) {

    constructor() : this(
        path = SshPath("~"),
        files = persistentListOf(),
        isLoading = true,
        loadingFile = null,
    )
}