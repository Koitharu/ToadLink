package org.koitharu.toadlink.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val dataStore = context.dataStore

    val showThumbnails: Flow<Boolean>
        get() = dataStore.data.map { it[SHOW_THUMBNAILS] ?: true }.distinctUntilChanged()

    val filesGridView: Flow<Boolean>
        get() = dataStore.data.map { it[FILES_GRID] ?: false }.distinctUntilChanged()

    suspend fun setShowThumbnails(value: Boolean) {
        dataStore.edit {
            it[SHOW_THUMBNAILS] = value
        }
    }

    suspend fun setFilesGridView(value: Boolean) {
        dataStore.edit {
            it[FILES_GRID] = value
        }
    }

    internal companion object {

        val SHOW_THUMBNAILS = booleanPreferencesKey("show_thumbs")
        val FILES_GRID = booleanPreferencesKey("files_grid")
    }
}