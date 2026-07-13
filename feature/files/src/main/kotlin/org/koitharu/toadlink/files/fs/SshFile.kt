package org.koitharu.toadlink.files.fs

import androidx.compose.runtime.Immutable
import okio.Path
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.files.data.XdgUserDir

@Immutable
data class SshFile(
    val host: DeviceDescriptor,
    val path: Path,
    val size: Long,
    val lastModified: Long,
    val owner: String,
    val symlinkTarget: String?,
    val type: MimeType,
    val xdgUserDir: XdgUserDir?,
) {

    val name: String = path.segments.last()

    val parentPath: Path?
        get() = path.parent

    val isSymlink: Boolean
        get() = symlinkTarget != null

    val isDirectory: Boolean
        get() = type == MimeType.DIRECTORY

    val uri: String = buildString {
        append("ssh://")
        append(host.hostname)
        append(':')
        append(host.port)
        if (!path.isAbsolute) {
            append('/')
        }
        append(path)
    }
}
