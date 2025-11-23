package org.koitharu.toadlink.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.koitharu.toadlink.actions.ui.editor.ActionEditorDestination
import org.koitharu.toadlink.actions.ui.editor.ActionEditorScreen
import org.koitharu.toadlink.adddevice.AddDeviceScreen
import org.koitharu.toadlink.control.ControlScreen
import org.koitharu.toadlink.finddevice.FindDeviceScreen
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
fun MainNav() {
    val backStack = rememberNavBackStack(FindDeviceDestination)
    CompositionLocalProvider(LocalRouter provides RouterImpl(backStack)) {
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
                    AddDeviceScreen(it.initialAddress)
                }
                entry<FindDeviceDestination> {
                    FindDeviceScreen()
                }
                entry<ActionEditorDestination> {
                    ActionEditorScreen(it.action)
                }
            }
        )
    }
}