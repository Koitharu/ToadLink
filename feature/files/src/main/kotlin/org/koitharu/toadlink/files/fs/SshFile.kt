package org.koitharu.toadlink.files.fs

import okio.Path

data class SshFile(
    val path: Path,
    val size: Long,
    val lastModified: Long,
    val owner: String,
    val symlinkTarget: String?,
    val type: MimeType,
) {

    val name: String = path.segments.last()

    val parentPath: Path?
        get() = path.parent

    val isSymlink: Boolean
        get() = symlinkTarget != null

    val isDirectory: Boolean
        get() = type == MimeType.DIRECTORY
}
