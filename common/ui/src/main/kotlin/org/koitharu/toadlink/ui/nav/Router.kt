package org.koitharu.toadlink.ui.nav

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey
import java.io.File

interface Router : MutableList<NavKey> {

    fun navigate(destination: NavKey) {
        add(destination)
    }

    fun changeRoot(destination: NavKey) {
        clear()
        add(destination)
    }

    fun back() {
        removeLastOrNull()
    }

    fun openFileInExternalApp(file: File)
}

val LocalRouter = compositionLocalOf<Router> { StubRouter() }

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
private class StubRouter : Router, MutableList<NavKey> by ArrayList() {

    override fun openFileInExternalApp(file: File) = Unit
}