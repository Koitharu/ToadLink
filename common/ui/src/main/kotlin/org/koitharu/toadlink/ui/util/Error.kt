package org.koitharu.toadlink.ui.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import org.koitharu.toadlink.ui.R
import java.net.SocketTimeoutException

@Composable
@ReadOnlyComposable
fun Throwable.displayMessage(): String = getDisplayMessage(LocalContext.current)

@Composable
fun Throwable.icon(): Painter = painterResource(getDisplayIcon(LocalContext.current))

fun Throwable.getDisplayMessage(context: Context): String {
    message?.let {
        if (it.isNotEmpty()) {
            return it
        }
    }
    return context.getString(R.string.error_generic)
}

@DrawableRes
fun Throwable.getDisplayIcon(context: Context): Int = when (this) {
    is SocketTimeoutException -> R.drawable.ic_disconnect
    else -> when {
        message?.contains(MSG_CONNECTING_PROBLEM) == true -> R.drawable.ic_disconnect
        else -> R.drawable.ic_error
    }
}

private const val MSG_CONNECTING_PROBLEM = "There was a problem while connecting to"