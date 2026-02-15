package org.koitharu.toadlink.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.koitharu.toadlink.actions.ui.editor.ActionEditorDestination
import org.koitharu.toadlink.actions.ui.editor.ActionEditorScreen
import org.koitharu.toadlink.control.ControlScreen
import org.koitharu.toadlink.editor.AddDeviceDestination
import org.koitharu.toadlink.editor.AddDeviceScreen
import org.koitharu.toadlink.editor.EditDeviceDestination
import org.koitharu.toadlink.finddevice.FindDeviceScreen
import org.koitharu.toadlink.settings.SettingsDestination
import org.koitharu.toadlink.settings.SettingsScreen
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
fun MainNav(initialNavKey: NavKey?) {
    val backStack = rememberNavBackStack(initialNavKey ?: FindDeviceDestination)
    CompositionLocalProvider(
        LocalRouter provides RouterImpl(
            backStack = backStack,
            context = LocalContext.current
        )
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<ControlDestination> {
                    ControlScreen(it.deviceId)
                }
                entry<AddDeviceDestination> {
                    AddDeviceScreen(0, it.initialAddress)
                }
                entry<EditDeviceDestination> {
                    AddDeviceScreen(it.deviceId, null)
                }
                entry<FindDeviceDestination> {
                    FindDeviceScreen()
                }
                entry<ActionEditorDestination> {
                    ActionEditorScreen(it.action)
                }
                entry<SettingsDestination> {
                    SettingsScreen()
                }
            }
        )
    }
}