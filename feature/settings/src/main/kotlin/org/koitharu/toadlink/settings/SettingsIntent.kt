package org.koitharu.toadlink.settings

internal sealed interface SettingsIntent {

    data class SetThumbnailsEnabled(val value: Boolean) : SettingsIntent

    data class SetFilesGridView(val value: Boolean) : SettingsIntent
}