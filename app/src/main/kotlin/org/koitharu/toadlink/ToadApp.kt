package org.koitharu.toadlink

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.koitharu.toadlink.client.SshConnectionManager

@HiltAndroidApp
class ToadApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SshConnectionManager.setLoggingEnabled(true) // TODO false for release
    }
}