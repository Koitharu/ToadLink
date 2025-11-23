package org.koitharu.toadlink.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import org.koitharu.toadlink.ui.R

@Composable
@ReadOnlyComposable
fun Throwable.displayMessage(): String = getDisplayMessage(LocalContext.current)

fun Throwable.getDisplayMessage(context: Context): String {
    message?.let {
        if (it.isNotEmpty()) {
            return it
        }
    }
    return context.getString(R.string.error_generic)
}