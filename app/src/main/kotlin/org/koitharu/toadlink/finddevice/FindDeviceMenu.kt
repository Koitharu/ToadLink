package org.koitharu.toadlink.finddevice

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.koitharu.toadlink.settings.SettingsDestination
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.OptionsMenu
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
fun FindDeviceMenu(
    handleIntent: MviIntentHandler<FindDeviceIntent>,
) = OptionsMenu {
    val router = LocalRouter.current
    DropdownMenuItem(
        text = { Text(stringResource(R.string.settings)) },
        onClick = {
            router.add(SettingsDestination)
        },
    )
}
