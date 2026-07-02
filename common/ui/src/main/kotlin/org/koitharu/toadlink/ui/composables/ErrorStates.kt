package org.koitharu.toadlink.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.util.displayMessage
import org.koitharu.toadlink.ui.util.icon

@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    message: String,
    icon: Painter = painterResource(R.drawable.ic_error),
    content: (@Composable () -> Unit)? = null,
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .then(modifier),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    Icon(
        modifier = Modifier
            .size(84.dp)
            .alpha(0.16f),
        painter = icon,
        contentDescription = null,
    )
    Text(
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = if (content != null) 12.dp else 0.dp,
        ),
        textAlign = TextAlign.Center,
        text = message,
        style = MaterialTheme.typography.titleMedium,
    )
    content?.invoke()
}

@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    error: Throwable,
) = ErrorState(
    modifier = modifier,
    message = error.displayMessage(),
    icon = error.icon(),
)

@Composable
@Preview
private fun ErrorStatePreview() = ErrorState(
    modifier = Modifier.fillMaxSize(),
    message = "Error occurred",
)
