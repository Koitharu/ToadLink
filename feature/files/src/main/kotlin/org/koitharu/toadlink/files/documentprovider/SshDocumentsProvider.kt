package org.koitharu.toadlink.files.documentprovider

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.provider.DocumentsProvider
import androidx.lifecycle.LifecycleCoroutineScope
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Scale
import coil3.toBitmap
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import org.koitharu.toadlink.client.SshConnectionManager
import org.koitharu.toadlink.core.util.runCatchingCancellable
import org.koitharu.toadlink.files.data.SshFileManager.Companion.fileManager
import org.koitharu.toadlink.files.fs.SshFile
import org.koitharu.toadlink.storage.DevicesRepository
import org.koitharu.toadlink.ui.R
import java.io.FileOutputStream

class SshDocumentsProvider : DocumentsProvider() {

    private val entryPoint by lazy {
        val appContext = checkNotNull(context).applicationContext
        EntryPointAccessors.fromApplication<SshDocumentProviderEntryPoint>(appContext)
    }
    private val treeCache = HashMap<String, List<SshFile>>()
    private val recentCache = HashMap<String, List<SshFile>>()
    private val rootCache = HashMap<String, SshFile>()

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor? {
        if ("w" in mode) {
            throw UnsupportedOperationException("Write mode not supported for this document")
        }

        val (readSide, writeSide) = ParcelFileDescriptor.createReliablePipe()

        val job = entryPoint.coroutineScope.launch(Dispatchers.IO) {
            val path = ('/' + documentId.substringAfter('/', "")).toPath()
            val deviceId = documentId.substringBefore('/').toInt()
            val connection = entryPoint.connectionManager.getConnection(
                entryPoint.devicesRepository.get(deviceId)
            )
            val source = connection.fileSystem.source(path).buffer()
            val outputStream = FileOutputStream(writeSide.fileDescriptor)
            val buffer = ByteArray(8192)
            var bytesRead: Int

            try {
                while (source.read(buffer).also { bytesRead = it } != -1) {
                    currentCoroutineContext().ensureActive()
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
                writeSide.close()
            } catch (e: Exception) {
                // If something fails, close with an error so the client knows
                try {
                    writeSide.closeWithError(e.message ?: "Unknown streaming error")
                } catch (_: IOException) {
                }
            } finally {
                // Always clean up your source stream
                try {
                    source.close()
                } catch (_: Exception) {
                }
                try {
                    outputStream.close()
                } catch (_: Exception) {
                }
            }
        }
        signal?.setOnCancelListener { job.cancel() }
        return readSide
    }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<out String?>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(projection ?: PROJECTION_DOCUMENTS)
        val notificationUri = DocumentsContract.buildChildDocumentsUri(AUTHORITY, parentDocumentId)
        cursor.setNotificationUri(requireContext().contentResolver, notificationUri)
        treeCache[parentDocumentId]?.let { files ->
            for (file in files) {
                cursor.addFileRow(file)
            }
        } ?: run {
            cursor.extras = Bundle(1).apply {
                putBoolean(DocumentsContract.EXTRA_LOADING, true)
            }
            loadTreeAsync(parentDocumentId, notificationUri)
        }
        return cursor
    }

    override fun queryRecentDocuments(rootId: String, projection: Array<out String?>?): Cursor {
        val cursor = MatrixCursor(projection ?: PROJECTION_DOCUMENTS)
        val notificationUri = DocumentsContract.buildChildDocumentsUri(AUTHORITY, rootId)
        cursor.setNotificationUri(requireContext().contentResolver, notificationUri)
        recentCache[rootId]?.let { files ->
            for (file in files) {
                cursor.addFileRow(file)
            }
        } ?: run {
            cursor.extras = Bundle(1).apply {
                putBoolean(DocumentsContract.EXTRA_LOADING, true)
            }
            loadRecentAsync(rootId, notificationUri)
        }
        return cursor
    }

    private fun MatrixCursor.addFileRow(
        file: SshFile
    ) = newRow().apply {
        add(
            Document.COLUMN_DOCUMENT_ID,
            "${file.host.id}${file.path}"
        )
        add(Document.COLUMN_DISPLAY_NAME, file.name)
        add(Document.COLUMN_MIME_TYPE, file.type.toAndroidString())
        add(Document.COLUMN_SIZE, file.size)
        add(Document.COLUMN_FLAGS, 0)
        add(Document.COLUMN_LAST_MODIFIED, file.lastModified)
    }

    override fun openDocumentThumbnail(
        documentId: String,
        sizeHint: Point?,
        signal: CancellationSignal?
    ): AssetFileDescriptor {
        val (readSide, writeSide) = ParcelFileDescriptor.createReliablePipe()
        val job = entryPoint.coroutineScope.launch(Dispatchers.Default) {
            val path = documentId.substringAfter('/', "")
            val deviceId = documentId.substringBefore('/').toInt()
            val size = sizeHint ?: THUMB_SIZE_FALLBACK
            val host = entryPoint.devicesRepository.get(deviceId)
            val request = ImageRequest.Builder(requireContext())
                .size(size.x, size.y)
                .scale(Scale.FILL)
                .data("ssh://${host.address}/$path")
                .build()
            when (val result = entryPoint.imageLoader.execute(request)) {
                is ErrorResult -> try {
                    writeSide.closeWithError(result.throwable.message)
                } catch (_: IOException) {
                }

                is SuccessResult -> runInterruptible(Dispatchers.IO) {
                    val bitmap = result.image.toBitmap()
                    ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                    }
                    bitmap.recycle()
                }
            }
        }
        signal?.setOnCancelListener { job.cancel() }
        return AssetFileDescriptor(readSide, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
    }

    override fun queryDocument(
        documentId: String,
        projection: Array<out String?>?
    ): Cursor {
        val cursor = MatrixCursor(projection ?: PROJECTION_DOCUMENTS)
        val notificationUri = DocumentsContract.buildChildDocumentsUri(AUTHORITY, documentId)
        cursor.setNotificationUri(requireContext().contentResolver, notificationUri)
        val file = rootCache[documentId] ?: run {
            runBlocking { loadDocAsync(documentId, notificationUri).await().getOrThrow() }
        }
        cursor.addFileRow(file)
        return cursor
    }

    override fun queryRoots(projection: Array<out String?>?): Cursor {
        val cursor = MatrixCursor(projection ?: PROJECTION_ROOTS)
        val connectedDevices = entryPoint.connectionManager.connections.value.keys
        val devices = runBlocking { entryPoint.devicesRepository.getAll() }
        for (d in devices) {
            cursor.newRow().apply {
                var flags = DocumentsContract.Root.FLAG_LIMITED_FUNCTIONALITY_WHEN_OFFLINE
                if (d in connectedDevices) {
                    flags = flags or DocumentsContract.Root.FLAG_SUPPORTS_EJECT
                }
                add(DocumentsContract.Root.COLUMN_ROOT_ID, d.id.toString())
                add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "${d.id}/home/${d.username}")
                add(DocumentsContract.Root.COLUMN_TITLE, d.displayName)
                add(DocumentsContract.Root.COLUMN_SUMMARY, d.username)
                add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_pc_phone)
                add(DocumentsContract.Root.COLUMN_FLAGS, flags)
            }
        }
        return cursor
    }

    override fun ejectRoot(rootId: String?) {
        val deviceId = rootId?.toIntOrNull() ?: return super.ejectRoot(rootId)
        runBlocking {
            entryPoint.connectionManager.disconnect(deviceId)
        }
    }

    override fun onCreate(): Boolean = true

    private fun loadTreeAsync(
        parentDocumentId: String,
        notifyUri: Uri,
    ) = entryPoint.coroutineScope.launch(Dispatchers.Default) {
        runCatchingCancellable {
            val path = ('/' + parentDocumentId.substringAfter('/', "")).toPath()
            val deviceId = parentDocumentId.substringBefore('/').toInt()
            val connection = entryPoint.connectionManager.getConnection(
                entryPoint.devicesRepository.get(deviceId)
            )
            val fm = connection.fileManager

            fm.listFiles(path, includeHidden = true)
        }.onSuccess { files ->
            withContext(Dispatchers.Main) {
                treeCache[parentDocumentId] = files
                context?.contentResolver?.notifyChange(notifyUri, null)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    private fun loadRecentAsync(
        parentDocumentId: String,
        notifyUri: Uri,
    ) = entryPoint.coroutineScope.launch(Dispatchers.Default) {
        runCatchingCancellable {
            val deviceId = parentDocumentId.substringBefore('/').toInt()
            val connection = entryPoint.connectionManager.getConnection(
                entryPoint.devicesRepository.get(deviceId)
            )
            val fm = connection.fileManager
            fm.getRecentlyUsed(20)
        }.onSuccess { files ->
            withContext(Dispatchers.Main) {
                recentCache[parentDocumentId] = files
                context?.contentResolver?.notifyChange(notifyUri, null)
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    private fun loadDocAsync(
        documentId: String,
        notifyUri: Uri,
    ) = entryPoint.coroutineScope.async(Dispatchers.Default) {
        runCatchingCancellable {
            val path = ('/' + documentId.substringAfter('/', "")).toPath()
            val deviceId = documentId.substringBefore('/').toInt()
            val connection = entryPoint.connectionManager.getConnection(
                entryPoint.devicesRepository.get(deviceId)
            )
            val fm = connection.fileManager
            fm.getFileInfo(path)
        }.onSuccess { file ->
            withContext(Dispatchers.Main) {
                rootCache[documentId] = file
            }
            context?.contentResolver?.notifyChange(notifyUri, null)
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SshDocumentProviderEntryPoint {
        val connectionManager: SshConnectionManager
        val devicesRepository: DevicesRepository
        val coroutineScope: LifecycleCoroutineScope
        val imageLoader: ImageLoader
    }

    private companion object {

        private const val AUTHORITY = "org.koitharu.toadlink.remotefiles"
        private val THUMB_SIZE_FALLBACK = Point(200, 200)

        private val PROJECTION_ROOTS = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_FLAGS,
        )
        private val PROJECTION_DOCUMENTS = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_FLAGS,
            Document.COLUMN_LAST_MODIFIED,
        )
    }
}