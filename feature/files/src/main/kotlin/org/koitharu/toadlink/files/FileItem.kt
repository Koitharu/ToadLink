package org.koitharu.toadlink.files

import android.text.format.DateUtils
import android.text.format.Formatter.formatFileSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koitharu.toadlink.files.fs.SshFile
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.util.themeAttributeSize
import org.koitharu.toadlink.files.R as featureR

@Composable
internal fun FileGridItem(
    file: SshFile,
    showThumbnail: Boolean,
    handleIntent: MviIntentHandler<FileManagerIntent>,
) {
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
        Column(
            modifier = Modifier
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.aspectRatio(1f),
            ) {
                val fileIcon = fileIcon(file)
                if (!showThumbnail || file.isDirectory) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(fileIcon),
                        contentDescription = null
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = file.uri,
                        contentDescription = null,
                        error = painterResource(fileIcon),
                        placeholder = painterResource(fileIcon)
                    )
                }
            }
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
            )
            FileSummary(
                modifier = Modifier.padding(top = 2.dp),
                file = file,
            )
            /*Box {
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
            }*/
        }
    }
}


@Composable
internal fun FileListItem(
    file: SshFile,
    showThumbnail: Boolean,
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
                val fileIcon = fileIcon(file)
                if (!showThumbnail || file.isDirectory) {
                    Icon(
                        painter = painterResource(fileIcon),
                        contentDescription = null
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier.size(24.dp),
                        model = file.uri,
                        contentDescription = null,
                        error = painterResource(fileIcon),
                        placeholder = painterResource(fileIcon)
                    )
                }
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
                tint = colorResource(R.color.toad),
            )
            Text(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .alignByBaseline(),
                text = file.symlinkTarget.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.toad),
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