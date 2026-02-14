package org.koitharu.toadlink.settings

import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsState(
    val isThumbnailsEnabled: Boolean,
    val isFilesGridView: Boolean,
) {

    constructor() : this(
        isThumbnailsEnabled = true,
        isFilesGridView = false,
    )
}