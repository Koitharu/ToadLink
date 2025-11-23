package org.koitharu.toadlink.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListHeader(
    modifier: Modifier = Modifier,
    text: String,
) = Text(
    modifier = Modifier
        .padding(
            vertical = 8.dp,
            horizontal = 16.dp
        )
        .then(modifier),
    text = text,
    style = MaterialTheme.typography.titleMedium
)

@Composable
fun ListHeader(
    modifier: Modifier = Modifier,
    text: String,
    trailing: @Composable RowScope.() -> Unit,
) = Row(
    modifier = Modifier
        .padding(end = 16.dp)
        .then(modifier),
    verticalAlignment = Alignment.CenterVertically,
) {
    ListHeader(
        modifier = Modifier
            .alignByBaseline()
            .weight(1f),
        text = text
    )
    trailing()
}