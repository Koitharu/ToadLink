package org.koitharu.toadlink.files

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.fs.SshFile
import org.koitharu.toadlink.core.fs.SshPath
import org.koitharu.toadlink.core.util.firstNotNull
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.files.FileManagerEffect.OnError
import org.koitharu.toadlink.files.FileManagerIntent.CancelFileTransfer
import org.koitharu.toadlink.files.FileManagerIntent.Navigate
import org.koitharu.toadlink.files.FileManagerIntent.NavigateUp
import org.koitharu.toadlink.files.FileManagerIntent.OpenFile
import org.koitharu.toadlink.files.data.LocalFileCache
import org.koitharu.toadlink.files.data.SshFileManager
import org.koitharu.toadlink.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class FileManagerViewModel @Inject constructor(
    private val connectionManager: SshConnectionManager,
    private val localFileCache: LocalFileCache,
) : MviViewModel<FileManagerState, FileManagerIntent, FileManagerEffect>(
    FileManagerState()
) {

    private val fileManager = connectionManager.activeConnection.map {
        it?.let { SshFileManager(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var loadJob: Job = viewModelScope.launch(Dispatchers.Default) {
        val home = runCatchingCancellable {
            fileManager.firstNotNull().getUserHome()
        }.getOrDefault(state.value.path)
        loadDirectory(home)
    }
    private var transferJob: Job? = null

    override fun handleIntent(intent: FileManagerIntent) = when (intent) {
        is Navigate -> navigate(intent.path)
        NavigateUp -> {
            val path = state.value.path.parent
            if (path != null) {
                navigate(path)
            } else {
                // TO
            }
        }

        CancelFileTransfer -> {
            transferJob?.cancel()
            transferJob = null
        }

        is OpenFile -> openFile(intent.file)
    }

    private fun openFile(file: SshFile) {
        transferJob?.cancel()
        transferJob = viewModelScope.launch(Dispatchers.Default) {
            state.update { it.copy(loadingFile = file.name) }
            try {
                val connection = connectionManager.awaitConnection()
                val target = localFileCache.createTempFile(file.name)
                target.sink().buffer().use {
                    connection.getFileContent(file.path.toString(), it)
                }
                sendEffect(FileManagerEffect.OpenExternal(localFileCache.getFileUri(target)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                sendEffect(OnError(e))
            } finally {
                state.update { it.copy(loadingFile = null) }
            }
        }
    }

    private fun navigate(path: SshPath) {
        val prevJob = loadJob
        loadJob = viewModelScope.launch(Dispatchers.Default) {
            prevJob.cancelAndJoin()
            loadDirectory(path)
        }
    }

    private suspend fun loadDirectory(path: SshPath) {
        state.update { it.copy(isLoading = true) }
        runCatchingCancellable {
            val fm = fileManager.firstNotNull()
            val absolutePath = if (path.isAbsolute) {
                path
            } else {
                fm.resolvePath(path.toString())
            }
            absolutePath to fm.listFiles(absolutePath, false)
                .sortedByDescending { it.isDirectory }
                .toImmutableList()
        }.onSuccess { (absolutePath, files) ->
            state.update {
                it.copy(
                    path = absolutePath,
                    files = files,
                    isLoading = false,
                )
            }
        }.onFailure { error ->
            sendEffect(OnError(error))
            state.update { it.copy(isLoading = false) }
        }
    }
}