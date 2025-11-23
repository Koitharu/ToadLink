package org.koitharu.toadlink.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.koitharu.toadlink.ui.nav.Router

class RouterImpl(
    private val backStack: NavBackStack<NavKey>,
) : Router {

    override fun navigate(destination: NavKey) {
        backStack.add(destination)
    }

    override fun back() {
        backStack.removeLastOrNull()
    }
}