package org.koitharu.toadlink.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koitharu.toadlink.settings.SettingsIntent.SetFilesGridView
import org.koitharu.toadlink.settings.SettingsIntent.SetThumbnailsEnabled
import org.koitharu.toadlink.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settings: AppSettings,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    init {
        observeSettings()
    }

    override fun handleIntent(intent: SettingsIntent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (intent) {
                is SetThumbnailsEnabled -> settings.setShowThumbnails(intent.value)
                is SetFilesGridView -> settings.setFilesGridView(intent.value)
            }
        }
    }

    private fun observeSettings() {
        settings.showThumbnails.onEach { value ->
            state.update { it.copy(isThumbnailsEnabled = value) }
        }.launchIn(viewModelScope + Dispatchers.Default)
        settings.filesGridView.onEach { value ->
            state.update { it.copy(isFilesGridView = value) }
        }.launchIn(viewModelScope + Dispatchers.Default)
    }
}