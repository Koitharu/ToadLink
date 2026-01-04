package org.koitharu.toadlink.ui.nav

import android.content.Context
import android.content.Intent

interface AppIntentFactory {

    fun findDeviceIntent(context: Context): Intent

    fun controlIntent(context: Context, deviceId: Int): Intent
}