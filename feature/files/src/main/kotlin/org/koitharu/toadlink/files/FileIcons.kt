package org.koitharu.toadlink.files

import androidx.annotation.DrawableRes
import org.koitharu.toadlink.files.data.XdgUserDir
import org.koitharu.toadlink.files.fs.SshFile

@DrawableRes
internal fun fileIcon(file: SshFile) = when {
    file.isDirectory -> dirIcon(file.xdgUserDir)
    file.type.isImage -> R.drawable.ic_file_image
    else -> R.drawable.ic_file_any
}

@DrawableRes
private fun dirIcon(userDir: XdgUserDir?) = when (userDir) {
    XdgUserDir.DOWNLOAD -> R.drawable.ic_dir_downloads
    XdgUserDir.MUSIC -> R.drawable.ic_dir_music
    XdgUserDir.DOCUMENTS -> R.drawable.ic_dir_documents
    XdgUserDir.TEMPLATES,
    XdgUserDir.PUBLICSHARE, // TODO
    XdgUserDir.PICTURES, // TODO
    XdgUserDir.VIDEOS, // TODO
    XdgUserDir.DESKTOP, // TODO
    null,
        -> R.drawable.ic_dir_any
}