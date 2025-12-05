package org.koitharu.toadlink.files

import android.content.Intent
import android.text.format.DateUtils
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.toadlink.core.fs.MimeType
import org.koitharu.toadlink.core.fs.SshFile
import org.koitharu.toadlink.core.fs.SshPath
import org.koitharu.toadlink.files.FileManagerEffect.OnError
import org.koitharu.toadlink.files.FileManagerEffect.OpenExternal
import org.koitharu.toadlink.files.FileManagerIntent.CancelFileTransfer
import org.koitharu.toadlink.files.FileManagerIntent.NavigateUp
import org.koitharu.toadlink.files.utils.formatFileSize
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
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
    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is OnError -> snackbarHostState.showSnackbar(
                    effect.error.getDisplayMessage(context)
                )

                is OpenExternal -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(effect.uri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.open_with))
                    )
                }
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
) {
    LazyColumn(
        contentPadding = contentPadding,
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
                FileItem(
                    file = file,
                    handleIntent = handleIntent,
                )
            }
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
private fun FileItem(
    file: SshFile,
    handleIntent: MviIntentHandler<FileManagerIntent>,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .heightIn(min = themeAttributeSize(android.R.attr.listPreferredItemHeight))
            .fillMaxWidth(),
        onClick = {
            if (file.isDirectory) {
                handleIntent(FileManagerIntent.Navigate(file.path))
            } else {
                handleIntent(FileManagerIntent.OpenFile(file))
            }
        }
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
                    painter = painterResource(fileIcon(file)),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                FileSummary(
                    modifier = Modifier.padding(top = 2.dp),
                    file = file,
                )
            }
            Box {
                IconButton(
                    onClick = { isMenuExpanded = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = stringResource(R.string.menu),
                    )
                }
                FileContextMenu(
                    file = file,
                    isExpanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    handleIntent = handleIntent,
                )
            }
        }
    }
}

@Composable
private fun FileSummary(
    modifier: Modifier,
    file: SshFile,
) = when {
    file.isSymlink -> {
        Row(
            modifier = modifier,
        ) {
            Icon(
                modifier = Modifier
                    .size(14.dp)
                    .alignByBaseline(),
                painter = painterResource(featureR.drawable.ic_arrow_link),
                contentDescription = null,
                tint = colorResource(R.color.teal_700),
            )
            Text(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .alignByBaseline(),
                text = file.symlinkTarget.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.teal_700),
            )
        }
    }

    else -> {
        val context = LocalContext.current
        val sizeString = remember(file.size) {
            formatFileSize(context, file.size)
        }
        val dateString = remember(file.lastModified) {
            DateUtils.getRelativeDateTimeString(
                context,
                file.lastModified,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0
            )
        }
        Text(
            modifier = modifier,
            text = buildString {
                append(sizeString)
                append(" · ")
                append(dateString)
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
@Preview
private fun PreviewFilesList() = FilesList(
    state = FileManagerState(
        path = SshPath("/home/user"),
        files = persistentListOf(
            SshFile(
                path = SshPath("/home/user/Documents"),
                size = 14304,
                lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                owner = "user",
                symlinkTarget = null,
                type = MimeType.DIRECTORY,
            ),
            SshFile(
                path = SshPath("/home/user/Downloads"),
                size = 14304,
                lastModified = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1),
                owner = "user",
                symlinkTarget = null,
                type = MimeType.DIRECTORY,
            ),
            SshFile(
                path = SshPath("/home/user/Music"),
                size = 3000,
                lastModified = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10),
                owner = "user",
                symlinkTarget = null,
                type = MimeType.DIRECTORY,
            ),
            SshFile(
                path = SshPath("/home/user/file.txt"),
                size = 403,
                lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                owner = "user",
                symlinkTarget = null,
                type = MimeType("text/plain"),
            ),
            SshFile(
                path = SshPath("/home/user/symlink"),
                size = 403,
                lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                owner = "user",
                symlinkTarget = "/var/log/log.txt",
                type = MimeType.UNKNOWN,
            ),
        ),
        isLoading = true,
        loadingFile = null,
    ),
    contentPadding = PaddingValues.Zero,
    handleIntent = MviIntentHandler.NoOp,
)