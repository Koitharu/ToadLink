package org.koitharu.toadlink.nav

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.koitharu.toadlink.files.data.LocalFileProvider
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.nav.Router
import java.io.File

class RouterImpl(
    private val backStack: NavBackStack<NavKey>,
    private val context: Context,
) : Router, MutableList<NavKey> by backStack {

    override fun openFileInExternalApp(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = FileProvider.getUriForFile(
            context,
            LocalFileProvider.AUTHORITY,
            file
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.open_with))
        )
    }
}