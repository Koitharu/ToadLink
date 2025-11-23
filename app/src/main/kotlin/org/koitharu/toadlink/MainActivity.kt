package org.koitharu.toadlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.toadlink.nav.MainNav
import org.koitharu.toadlink.ui.theme.ToadLinkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToadLinkTheme {
                MainNav()
            }
        }
    }
}
