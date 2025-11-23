package org.koitharu.toadlink.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DotIndicator(
    modifier: Modifier = Modifier,
    color: Color,
) = Surface(
    modifier = Modifier
        .size(4.dp)
        .then(modifier),
    shape = CircleShape,
    color = color,
    content = {}
)

@Preview
@Composable
private fun DotIndicatorPreview() = DotIndicator(
    color = Color.Red
)
