package org.koitharu.toadlink.files.fs


@JvmInline
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

    companion object {

        val DIRECTORY = MimeType("inode/directory")
        val UNKNOWN = MimeType("application/octet-stream")

        fun String.toMimeTypeOrNull(): MimeType? = if (REGEX_MIME.matches(this)) {
            MimeType(lowercase())
        } else {
            null
        }

        private const val TYPE_IMAGE = "image"
        private val REGEX_MIME = Regex("^\\w+/([-+.\\w]+|\\*)$", RegexOption.IGNORE_CASE)
    }
}

