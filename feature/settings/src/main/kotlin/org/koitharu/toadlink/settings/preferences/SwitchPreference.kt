package org.koitharu.toadlink.settings.preferences

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun SwitchPreference(
    title: String,
    summary: String? = null,
    isChecked: Boolean,
    onClick: () -> Unit,
) = Preference(
    title = title,
    summary = summary,
    onClick = onClick,
) {
    Switch(
        checked = isChecked,
        onCheckedChange = null,
    )
}

@Preview
@Composable
private fun PreviewSwitchPreference() = MaterialExpressiveTheme {
    SwitchPreference(
        title = "Title",
        summary = "Preference subtitle",
        isChecked = true,
        onClick = { /* no-op */ },
    )
}