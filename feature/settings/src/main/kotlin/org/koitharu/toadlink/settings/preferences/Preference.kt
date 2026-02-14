package org.koitharu.toadlink.settings.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PreferenceTitle(
    text: String,
) = Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
)

@Composable
internal fun PreferenceSummary(
    text: String,
) = Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
)

@Composable
internal fun Preference(
    title: String,
    summary: String? = null,
    onClick: () -> Unit,
    content: (@Composable RowScope.() -> Unit)? = null,
) = Row(
    modifier = Modifier
        .clickable(onClick = onClick)
        .fillMaxWidth()
        .heightIn(min = 56.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Column(
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            )
            .weight(1f),
        verticalArrangement = Arrangement.Center,
    ) {
        PreferenceTitle(title)
        if (!summary.isNullOrBlank()) {
            PreferenceSummary(summary)
        }
    }
    if (content != null) {
        content()
        Spacer(modifier = Modifier.width(16.dp))
    }
}