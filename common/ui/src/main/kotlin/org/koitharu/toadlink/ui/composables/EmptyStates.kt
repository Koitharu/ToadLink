package org.koitharu.toadlink.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.koitharu.toadlink.ui.R

@Composable
fun EmptyState(
    modifier: Modifier,
    message: String,
    icon: Painter = painterResource(R.drawable.ic_menu),
    content: (@Composable () -> Unit)? = null,
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    Icon(
        painter = icon,
        contentDescription = null,
    )
    Text(
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = if (content != null) 12.dp else 0.dp,
        ),
        text = message,
        style = MaterialTheme.typography.titleMedium,
    )
    content?.invoke()
}

@Composable
fun UnsupportedFunctionalityState() {
    TODO()
}
