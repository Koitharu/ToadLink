package org.koitharu.toadlink.ui.nav

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey

interface Router {

    fun navigate(destination: NavKey)

    fun back()
}

val LocalRouter = compositionLocalOf<Router> { StubRouter() }

private class StubRouter : Router {

    override fun navigate(destination: NavKey) = Unit

    override fun back() = Unit
}