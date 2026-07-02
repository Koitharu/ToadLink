package org.koitharu.toadlink.actions.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koitharu.toadlink.core.RemoteAction
import org.koitharu.toadlink.ui.R

@Composable
internal fun RunConfirmationDialog(
    action: RemoteAction,
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
                    text = action.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.action_run_confirmation))
                        appendLine()
                        appendLine()
                        withStyle(SpanStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)) {
                            append(action.cmdline)
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
                            text = stringResource(R.string.execute),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
)