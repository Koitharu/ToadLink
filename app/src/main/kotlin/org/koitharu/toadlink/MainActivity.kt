package org.koitharu.toadlink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.IntentCompat
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.toadlink.nav.ControlDestination
import org.koitharu.toadlink.nav.FindDeviceDestination
import org.koitharu.toadlink.nav.MainNav
import org.koitharu.toadlink.ui.nav.AppIntentFactory
import org.koitharu.toadlink.ui.theme.ToadLinkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialNavKey =
            IntentCompat.getParcelableExtra(intent, ARG_DESTINATION, NavKey::class.java)
        enableEdgeToEdge()
        setContent {
            ToadLinkTheme {
                MainNav(
                    initialNavKey
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // TODO handle navigation
    }

    companion object : AppIntentFactory {

        private const val ARG_DESTINATION = "destination"

        override fun findDeviceIntent(context: Context) = getIntent(context, FindDeviceDestination)

        override fun controlIntent(
            context: Context,
            deviceId: Int,
        ): Intent = getIntent(context, ControlDestination(deviceId))

        private fun <T> getIntent(
            context: Context,
            destination: T,
        ): Intent where T : Parcelable, T : NavKey = Intent(context, MainActivity::class.java)
            .putExtra(ARG_DESTINATION, destination)
    }
}
