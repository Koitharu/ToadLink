package org.koitharu.toadlink.files

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okio.Path.Companion.toPath
import org.koitharu.toadlink.files.FileManagerEffect.OnError
import org.koitharu.toadlink.files.FileManagerEffect.OpenExternal
import org.koitharu.toadlink.files.FileManagerIntent.CancelFileTransfer
import org.koitharu.toadlink.files.FileManagerIntent.NavigateUp
import org.koitharu.toadlink.files.data.XdgUserDir
import org.koitharu.toadlink.files.fs.MimeType
import org.koitharu.toadlink.files.fs.SshFile
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter
import org.koitharu.toadlink.ui.util.getDisplayMessage
import org.koitharu.toadlink.ui.util.themeAttributeSize
import java.util.concurrent.TimeUnit
import org.koitharu.toadlink.files.R as featureR

@Composable
fun FileManagerContent(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = hiltViewModel<FileManagerViewModel>()
    val state by viewModel.collectState()
    val context = LocalContext.current
    val router = LocalRouter.current
    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is OnError -> snackbarHostState.showSnackbar(
                    effect.error.getDisplayMessage(context)
                )

                is OpenExternal -> router.openFileInExternalApp(effect.file)
            }
        }.launchIn(this)
    }
    BackHandler(
        enabled = state.path.parent != null,
    ) {
        viewModel.handleIntent(NavigateUp)
    }
    FilesList(
        contentPadding = contentPadding,
        state = state,
        handleIntent = viewModel,
    )
    state.loadingFile?.let { loadingFile ->
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(CancelFileTransfer) },
            title = { Text(loadingFile) },
            text = {
                Box {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.handleIntent(CancelFileTransfer) }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun FilesList(
    state: FileManagerState,
    contentPadding: PaddingValues,
    handleIntent: MviIntentHandler<FileManagerIntent>,
) = if (state.gridView) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = contentPadding,
    ) {
        filesGrid(state, handleIntent)
    }
} else {
    LazyColumn(
        contentPadding = contentPadding,
    ) {
        filesList(state, handleIntent)
    }
}

private fun LazyGridScope.filesGrid(
    state: FileManagerState,
    handleIntent: MviIntentHandler<FileManagerIntent>
) {
    item(
        span = { GridItemSpan(maxCurrentLineSpan) },
        contentType = "header"
    ) {
        Header(
            path = state.path.toString(),
            isLoading = state.isLoading,
            handleIntent = handleIntent,
        )
    }
    if (state.files.isEmpty() && !state.isLoading) {
        item(
            span = { GridItemSpan(maxCurrentLineSpan) },
            contentType = "empty"
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.empty_directory_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    } else {
        items(
            items = state.files,
            key = { it.path.toString() },
            contentType = { "file" },
        ) { file ->
            FileGridItem(
                file = file,
                showThumbnail = state.showThumbnails,
                handleIntent = handleIntent,
            )
        }
    }
}

private fun LazyListScope.filesList(
    state: FileManagerState,
    handleIntent: MviIntentHandler<FileManagerIntent>
) {
    item(
        contentType = "header"
    ) {
        Header(
            path = state.path.toString(),
            isLoading = state.isLoading,
            handleIntent = handleIntent,
        )
    }
    if (state.files.isEmpty() && !state.isLoading) {
        item(
            contentType = "empty"
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.empty_directory_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    } else {
        items(
            items = state.files,
            key = { it.path.toString() },
            contentType = { "file" },
        ) { file ->
            FileListItem(
                file = file,
                showThumbnail = state.showThumbnails,
                handleIntent = handleIntent,
            )
        }
    }
}

@Composable
private fun Header(
    path: String,
    isLoading: Boolean,
    handleIntent: MviIntentHandler<FileManagerIntent>,
) = Surface(
    modifier = Modifier
        .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeightSmall))
        .fillMaxWidth(),
    onClick = { handleIntent(NavigateUp) }
) {
    Row(
        modifier = Modifier
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Icon(
                painter = painterResource(featureR.drawable.ic_dir_up),
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
        ) {
            Text(
                text = path,
                style = MaterialTheme.typography.titleSmall
            )
        }
        AnimatedVisibility(isLoading) {
            LoadingIndicator(
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
@Preview
private fun PreviewFilesList() = FilesList(
    state = FileManagerState(
        path = "/home/user".toPath(),
        files = persistentListOf(
            SshFile(
                path = "/home/user/Documents".toPath(),
                size = 14304,
                lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                owner = "user",
                symlinkTarget = null,
                type = MimeType.DIRECTORY,
                xdgUserDir = XdgUserDir.DOCUMENTS,
            ),
            SshFile(
                path = "/home/user/Downloads".toPath(),
                size = 14304,
                lastModified = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1),
                owner = "user",
                symlinkTarget = null,
                type = MimeType.DIRECTORY,
                xdgUserDir = XdgUserDir.DOWNLOAD,
            ),
            SshFile(
                path = "/home/user/Music".toPath(),
                size = 3000,
                lastModified = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10),
                owner = "user",
                symlinkTarget = null,
                type = MimeType.DIRECTORY,
                xdgUserDir = XdgUserDir.MUSIC,
            ),
            SshFile(
                path = "/home/user/file.txt".toPath(),
                size = 403,
                lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                owner = "user",
                symlinkTarget = null,
                type = MimeType("text/plain"),
                xdgUserDir = null,
            ),
            SshFile(
                path = "/home/user/symlink".toPath(),
                size = 403,
                lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                owner = "user",
                symlinkTarget = "/var/log/log.txt",
                type = MimeType.UNKNOWN,
                xdgUserDir = null,
            ),
        ),
        isLoading = true,
        loadingFile = null,
        showThumbnails = false,
        gridView = true,
    ),
    contentPadding = PaddingValues.Zero,
    handleIntent = MviIntentHandler.NoOp,
)