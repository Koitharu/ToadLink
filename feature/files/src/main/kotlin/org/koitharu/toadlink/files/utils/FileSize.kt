package org.koitharu.toadlink.files.utils

import android.content.Context
import org.koitharu.toadlink.ui.R
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

fun formatFileSize(context: Context, bytes: Long): String {
    val units = context.getString(R.string.file_size_suffixes).split('|')
    if (bytes <= 0L) {
        return "0 ${units.first()}"
    }
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return buildString {
        append(
            DecimalFormat("#,##0.#").format(
                bytes / 1024.0.pow(digitGroups.toDouble()),
            ),
        )
        val unit = units.getOrNull(digitGroups)
        if (unit != null) {
            append(' ')
            append(unit)
        }
    }
}
