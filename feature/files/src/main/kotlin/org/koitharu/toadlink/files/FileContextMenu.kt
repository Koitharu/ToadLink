package org.koitharu.toadlink.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import okio.Path.Companion.toPath
import org.koitharu.toadlink.files.FileManagerIntent.DeleteFile
import org.koitharu.toadlink.files.FileManagerIntent.RenameFile
import org.koitharu.toadlink.files.FileManagerIntent.SaveFile
import org.koitharu.toadlink.files.fs.MimeType
import org.koitharu.toadlink.files.fs.SshFile
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import java.util.concurrent.TimeUnit

@Composable
internal fun FileContextMenu(
    file: SshFile,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    handleIntent: MviIntentHandler<FileManagerIntent>,
) {
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var isRenameDialogVisible by remember { mutableStateOf(false) }
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument(file.type.toString())
    ) { result ->
        if (result != null) {
            handleIntent(SaveFile(file, result))
        }
    }
    if (isDeleteDialogVisible) {
        DeleteConfirmationDialog(
            file = file,
            onDismissRequest = { isDeleteDialogVisible = false },
            onConfirm = { handleIntent(DeleteFile(file)) }
        )
    }
    if (isRenameDialogVisible) {
        RenameConfirmationDialog(
            file = file,
            onDismissRequest = { isRenameDialogVisible = false },
            onConfirm = { handleIntent(RenameFile(file, it)) }
        )
    }
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.get_and_open)) },
            onClick = {
                handleIntent(FileManagerIntent.OpenFile(file))
                onDismissRequest()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.get_and_save)) },
            onClick = {
                saveFileLauncher.launch(file.name)
                onDismissRequest()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.get_and_share)) },
            onClick = {
                handleIntent(FileManagerIntent.ShareFile(file))
                onDismissRequest()
            },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rename)) },
            onClick = {
                isRenameDialogVisible = true
                onDismissRequest()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete)) },
            onClick = {
                isDeleteDialogVisible = true
                onDismissRequest()
            },
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    file: SshFile,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) = BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    content = {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.delete_file),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.file_removal_confirmation, file.name))
                        appendLine()
                        appendLine()
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.undone_notice))
                        }
                    },
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        },
                        modifier = Modifier.alignByBaseline(),
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            maxLines = 1,
                        )
                    }
                    TextButton(
                        onClick = {
                            onConfirm()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.textButtonColors().copy(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.alignByBaseline(),
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
)


@Composable
private fun RenameConfirmationDialog(
    file: SshFile,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) = BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    content = {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            var newName by remember { mutableStateOf(file.name) }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.rename_file),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.rename_file_message, file.name),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = newName,
                    singleLine = true,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.file_name)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        },
                        modifier = Modifier.alignByBaseline(),
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            maxLines = 1,
                        )
                    }
                    TextButton(
                        onClick = {
                            onConfirm(newName.trim())
                            onDismissRequest()
                        },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier.alignByBaseline(),
                    ) {
                        Text(
                            text = stringResource(R.string.rename),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
)

@Preview(showBackground = true)
@Composable
fun DeleteDialogPreview() = DeleteConfirmationDialog(
    file = SshFile(
        path = "/home/user/file.txt".toPath(),
        size = 403,
        lastModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
        owner = "user",
        symlinkTarget = null,
        type = MimeType("text/plain"),
        xdgUserDir = null,
    ),
    onDismissRequest = { /* no-op */ },
    onConfirm = { /* no-op */ },
)