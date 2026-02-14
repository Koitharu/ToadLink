package org.koitharu.toadlink.settings.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCategory(
    title: String,
) = Text(
    modifier = Modifier.padding(
        horizontal = 16.dp,
        vertical = 6.dp,
    ),
    text = title,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.primary
)