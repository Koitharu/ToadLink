package org.koitharu.toadlink.settings.screens

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import org.koitharu.toadlink.settings.SettingsIntent
import org.koitharu.toadlink.settings.SettingsIntent.SetFilesGridView
import org.koitharu.toadlink.settings.SettingsIntent.SetThumbnailsEnabled
import org.koitharu.toadlink.settings.SettingsState
import org.koitharu.toadlink.settings.preferences.SwitchPreference
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler

internal fun LazyListScope.fileManagerPreferenceScreen(
    state: SettingsState,
    handleIntent: MviIntentHandler<SettingsIntent>
) {
    item {
        SwitchPreference(
            title = stringResource(R.string.show_thumbnails),
            summary = stringResource(R.string.show_thumbnails_summary),
            isChecked = state.isThumbnailsEnabled,
            onClick = { handleIntent(SetThumbnailsEnabled(!state.isThumbnailsEnabled)) }
        )
    }
    item {
        SwitchPreference(
            title = stringResource(R.string.show_files_in_grid),
            summary = stringResource(R.string.show_files_in_grid_summary),
            isChecked = state.isFilesGridView,
            onClick = { handleIntent(SetFilesGridView(!state.isFilesGridView)) }
        )
    }
}