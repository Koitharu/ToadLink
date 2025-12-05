package org.koitharu.toadlink.files

import androidx.annotation.DrawableRes
import org.koitharu.toadlink.core.fs.SshFile

@DrawableRes
internal fun fileIcon(file: SshFile) = when {
    file.isDirectory -> R.drawable.ic_dir_any
    file.type.isImage -> R.drawable.ic_file_image
    else -> R.drawable.ic_file_any
}