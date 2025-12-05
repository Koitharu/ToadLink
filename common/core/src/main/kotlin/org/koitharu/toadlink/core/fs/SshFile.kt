package org.koitharu.toadlink.core.fs

data class SshFile(
    val path: SshPath,
    val size: Long,
    val lastModified: Long,
    val owner: String,
    val symlinkTarget: String?,
    val type: MimeType,
) {

    val name: String = path.lastSegment()

    val parentPath: SshPath?
        get() = path.parent

    val isSymlink: Boolean
        get() = symlinkTarget != null

    val isDirectory: Boolean
        get() = type == MimeType.DIRECTORY
}
