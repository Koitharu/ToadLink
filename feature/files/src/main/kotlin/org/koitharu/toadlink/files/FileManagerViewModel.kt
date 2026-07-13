package org.koitharu.toadlink.files

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import okio.FileNotFoundException
import okio.Path
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.client.scp
import org.koitharu.toadlink.core.DeviceDescriptor
import org.koitharu.toadlink.core.util.firstNotNull
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.files.FileManagerEffect.OnError
import org.koitharu.toadlink.files.FileManagerEffect.OnFileSaved
import org.koitharu.toadlink.files.FileManagerEffect.OpenExternal
import org.koitharu.toadlink.files.FileManagerEffect.OpenShare
import org.koitharu.toadlink.files.FileManagerIntent.CancelFileTransfer
import org.koitharu.toadlink.files.FileManagerIntent.DeleteFile
import org.koitharu.toadlink.files.FileManagerIntent.Navigate
import org.koitharu.toadlink.files.FileManagerIntent.NavigateUp
import org.koitharu.toadlink.files.FileManagerIntent.OpenFile
import org.koitharu.toadlink.files.FileManagerIntent.RenameFile
import org.koitharu.toadlink.files.FileManagerIntent.SaveFile
import org.koitharu.toadlink.files.FileManagerIntent.ShareFile
import org.koitharu.toadlink.files.FileManagerIntent.TransferFileIntent
import org.koitharu.toadlink.files.data.LocalFileCache
import org.koitharu.toadlink.files.data.SshFileManager
import org.koitharu.toadlink.files.fs.SshFile
import org.koitharu.toadlink.settings.AppSettings
import org.koitharu.toadlink.ui.mvi.MviViewModel

@HiltViewModel(assistedFactory = FileManagerViewModel.Factory::class)
internal class FileManagerViewModel @AssistedInject constructor(
    @Assisted host: DeviceDescriptor,
    private val connectionManager: SshConnectionManager,
    private val localFileCache: LocalFileCache,
    private val settings: AppSettings,
    private val contentResolver: ContentResolver,
) : MviViewModel<FileManagerState, FileManagerIntent, FileManagerEffect>(
    FileManagerState()
) {

    private val fileManager = connectionManager.observeConnection(host).map {
        it?.let { SshFileManager(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var loadJob: Job = viewModelScope.launch(Dispatchers.Default) {
        val home = runCatchingCancellable {
            fileManager.firstNotNull().getUserHome()
        }.getOrDefault(state.value.path)
        loadDirectory(home)
    }
    private var transferJob: Job? = null

    init {
        observeSettings()
    }

    override fun handleIntent(intent: FileManagerIntent) = when (intent) {
        is Navigate -> navigate(intent.path)
        NavigateUp -> {
            val path = state.value.path.parent
            if (path != null) {
                navigate(path)
            } else {
                // TODO
            }
        }

        CancelFileTransfer -> {
            transferJob?.cancel()
            transferJob = null
        }

        is TransferFileIntent -> transferFile(intent)
        is DeleteFile -> deleteFile(intent.file)
        is RenameFile -> renameFIle(intent.file, intent.newName)
    }

    private fun transferFile(intent: TransferFileIntent) {
        transferJob?.cancel()
        transferJob = viewModelScope.launch(Dispatchers.Default) {
            val file = intent.file
            state.update { it.copy(loadingFile = file.name) }
            try {
                val connection = connectionManager.getConnection(file.host)
                if (intent is SaveFile) {
                    contentResolver.openOutputStream(intent.target)?.use {
                        connection.scp(file.path, it)
                        sendEffect(OnFileSaved(file.name))
                    } ?: throw FileNotFoundException(intent.target.toString())
                } else {
                    val target = localFileCache.createTempFile(file.name)
                    connection.scp(file.path, target)
                    when (intent) {
                        is OpenFile -> sendEffect(OpenExternal(target))
                        is ShareFile -> sendEffect(OpenShare(target, file.type))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                sendEffect(OnError(e))
            } finally {
                state.update { it.copy(loadingFile = null) }
            }
        }
    }

    private fun deleteFile(file: SshFile) {
        viewModelScope.launch(Dispatchers.Default) {
            runCatchingCancellable {
                val connection = connectionManager.getConnection(file.host)
                runInterruptible(Dispatchers.IO) {
                    connection.fileSystem.delete(file.path)
                }
                state.updateAndGet {
                    it.copy(files = it.files.removing(file))
                }.path
            }.onSuccess { path ->
                navigate(path)
            }.onFailure { e ->
                sendEffect(OnError(e))
            }
        }
    }

    private fun renameFIle(file: SshFile, newName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            runCatchingCancellable {
                val connection = connectionManager.getConnection(file.host)
                val targetPath = checkNotNull(file.parentPath) / newName
                runInterruptible(Dispatchers.IO) {
                    connection.fileSystem.atomicMove(file.path, targetPath)
                }
                state.updateAndGet {
                    val index = it.files.indexOf(file)
                    if (index in it.files.indices) {
                        it.copy(files = it.files.replacingAt(index, file.copy(path = targetPath)))
                    } else {
                        it
                    }
                }.path
            }.onSuccess { path ->
                navigate(path)
            }.onFailure { e ->
                sendEffect(OnError(e))
            }
        }
    }

    private fun navigate(path: Path) {
        val prevJob = loadJob
        loadJob = viewModelScope.launch(Dispatchers.Default) {
            prevJob.cancelAndJoin()
            loadDirectory(path)
        }
    }

    private suspend fun loadDirectory(path: Path) {
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
                .toPersistentList()
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

    private fun observeSettings() {
        viewModelScope.launch(Dispatchers.Default) {
            settings.showThumbnails.collect { value ->
                state.update { it.copy(showThumbnails = value) }
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            settings.filesGridView.collect { value ->
                state.update { it.copy(gridView = value) }
            }
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(host: DeviceDescriptor): FileManagerViewModel
    }
}