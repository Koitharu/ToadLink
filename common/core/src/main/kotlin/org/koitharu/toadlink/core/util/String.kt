package org.koitharu.toadlink.core.util

fun String.nullIfEmpty() = ifEmpty { null }

fun String.lineCount(): Int {
    return count { x -> x == '\n' }
}

fun Int.formatTimeSeconds(): String {
    if (this == 0) {
        return "00:00"
    }
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return if (hours == 0) {
        "%02d:%02d".format(minutes, seconds)
    } else {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

private val SPLIT_REGEX = Regex("""(?<!\\)(?=\s)(?=(?:[^"]*"[^"]*")*[^"]*$)""")

fun String.splitByWhitespace(): List<String> = split(SPLIT_REGEX)
    .mapNotNull { group -> group.trim().takeUnless { it.isEmpty() } }

private val OCTAL_REGEX = Regex("""\\([0-7]{1,3})""")

fun String.unescape(): String {
    val bytes = mutableListOf<Byte>()
    val sb = StringBuilder()
    var lastIndex = 0

    for (match in OCTAL_REGEX.findAll(this)) {
        sb.append(this.substring(lastIndex, match.range.first))

        val value = match.groupValues[1].toInt(8)
        bytes.add(value.toByte())
        val next = match.next()
        val shouldFlush = next == null || next.range.first != match.range.last + 1
        if (shouldFlush) {
            sb.append(bytes.toByteArray().toString(Charsets.UTF_8))
            bytes.clear()
        }
        lastIndex = match.range.last + 1
    }
    sb.append(this.substring(lastIndex))
    return sb.toString()
}