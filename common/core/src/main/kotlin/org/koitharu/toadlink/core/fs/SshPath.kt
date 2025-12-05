package org.koitharu.toadlink.core.fs

@JvmInline
value class SshPath(
    private val path: String,
) {

    override fun toString(): String = path

    val parent: SshPath?
        get() {
            val p = path.substringBeforeLast(SEPARATOR_CHAR)
            return if (p.isEmpty()) {
                null
            } else {
                SshPath(p)
            }
        }

    val isAbsolute: Boolean
        get() = path.startsWith(SEPARATOR_CHAR)

    fun lastSegment(): String = path.substringAfterLast(SEPARATOR_CHAR)

    fun resolve(child: String): SshPath = if (child.startsWith(SEPARATOR_CHAR)) {
        SshPath(child)
    } else {
        SshPath(
            buildString(path.length + child.length + 1) {
                append(path)
                append(SEPARATOR_CHAR)
                append(child)
                if (child.endsWith(SEPARATOR_CHAR)) {
                    deleteAt(lastIndex)
                }
            }
        )
    }

    companion object {

        const val SEPARATOR_CHAR = '/'

        val ROOT = SshPath(SEPARATOR_CHAR.toString())
        val HOME = SshPath("~")
    }
}