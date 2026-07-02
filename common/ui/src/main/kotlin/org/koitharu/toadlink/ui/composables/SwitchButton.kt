package org.koitharu.toadlink.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SwitchButton(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) = Surface(
    modifier = Modifier
        .clearAndSetSemantics {
            role = Role.Switch
            toggleableState = ToggleableState(checked)
        }
        .then(modifier),
    color = Color.Transparent,
    enabled = enabled,
    shape = MaterialTheme.shapes.small,
    onClick = onClick,
) {
    Row(
        modifier = Modifier.heightIn(min = 54.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f),
            text = text,
            color = if (enabled) {
                LocalContentColor.current
            } else {
                LocalContentColor.current.copy(alpha = 0.38f)
            },
            style = MaterialTheme.typography.titleSmall,
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = null,
        )
        Spacer(
            modifier = Modifier.width(8.dp),
        )
    }
}

@Preview("Checked")
@Composable
private fun SwitchButtonPreviewChecked() = SwitchButton(
    text = "Lorem ispum",
    checked = true,
    onClick = { /* no-op */ }
)

@Preview("Unchecked")
@Composable
private fun SwitchButtonPreviewUnchecked() = SwitchButton(
    text = "Lorem ispum",
    checked = false,
    onClick = { /* no-op */ }
)

@Preview("Disabled")
@Composable
private fun SwitchButtonPreviewDisabled() = SwitchButton(
    text = "Lorem ispum",
    checked = true,
    enabled = false,
    onClick = { /* no-op */ }
)