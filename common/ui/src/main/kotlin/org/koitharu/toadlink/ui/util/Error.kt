package org.koitharu.toadlink.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koitharu.toadlink.ui.R

@Composable
fun Throwable.displayMessage(): String {
    val context = LocalContext.current
    return remember(this) {
        getDisplayMessage(context)
    }
}

fun Throwable.getDisplayMessage(context: Context): String {
    message?.let {
        if (it.isNotEmpty()) {
            return it
        }
    }
    return context.getString(R.string.error_generic)
}