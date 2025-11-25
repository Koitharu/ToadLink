package org.koitharu.toadlink.ui.nav

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey

interface Router : MutableList<NavKey> {

    fun navigate(destination: NavKey)

    fun changeRoot(destination: NavKey)

    fun back()
}

val LocalRouter = compositionLocalOf<Router> { StubRouter() }

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
private class StubRouter : Router, MutableList<NavKey> by ArrayList() {

    override fun navigate(destination: NavKey) = Unit

    override fun changeRoot(destination: NavKey) = Unit

    override fun back() = Unit
}