package org.koitharu.toadlink.files.fs

import androidx.compose.runtime.Immutable
import coil3.annotation.InternalCoilApi
import coil3.util.MimeTypeMap


@JvmInline
@Immutable
value class MimeType(private val value: String) {

    val type: String?
        get() = value.substringBefore('/', "").takeIfSpecified()

    val subtype: String?
        get() = value.substringAfterLast('/', "").takeIfSpecified()

    private fun String.takeIfSpecified(): String? = takeUnless {
        it.isEmpty() || it == "*"
    }

    override fun toString(): String = value

    val isImage: Boolean
        get() = type == TYPE_IMAGE

    val isVideo: Boolean
        get() = type == TYPE_VIDEO

    val isAudio: Boolean
        get() = type == TYPE_AUDIO

    val isArchive: Boolean
        get() = type == TYPE_APPLICATION && subtype in SUBTYPES_ARCHIVE

    companion object {

        const val ANY = "*"

        val DIRECTORY = MimeType("inode/directory")
        val UNKNOWN = MimeType("application/octet-stream")

        fun String.toMimeTypeOrNull(): MimeType? = if (REGEX_MIME.matches(this)) {
            MimeType(substringBefore(';').lowercase())
        } else {
            null
        }

        @OptIn(InternalCoilApi::class)
        fun fromFileName(fileName: String): MimeType? {
            val extension = getNormalizedExtension(fileName) ?: return null
            return MimeTypeMap.getMimeTypeFromExtension(extension)?.toMimeTypeOrNull()
        }

        private const val TYPE_IMAGE = "image"
        private const val TYPE_VIDEO = "video"
        private const val TYPE_AUDIO = "audio"
        private const val TYPE_FONT = "font"
        private const val TYPE_TEXT = "text"
        private const val TYPE_APPLICATION = "application"
        private val REGEX_MIME = Regex("^\\w+/([-+.\\w]+|\\*)$", RegexOption.IGNORE_CASE)
        private val SUBTYPES_ARCHIVE = setOf(
            "zip",
            "vnd.rar",
            "x-7z-compressed",
            "x-tar",
            "gzip",
            "x-gzip",
            "x-bzip",
            "x-freearc",
        )

        private fun getNormalizedExtension(name: String): String? = name
            .lowercase()
            .removeSuffix("~")
            .removeSuffix(".tmp")
            .substringAfterLast('.', "")
            .takeIf { it.length in 1..5 }
    }
}

