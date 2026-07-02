package org.koitharu.toadlink.mpris.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isUnspecified

private class CoverPlaceholderPainter(
    private val background: Color,
    private val foreground: Painter,
    private val iconSize: Size,
) : Painter() {

    override val intrinsicSize = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(color = background)
        drawPainter(painter = foreground, alpha = 0.16f)
    }

    private fun DrawScope.drawPainter(painter: Painter?, alpha: Float) {
        if (painter == null || alpha <= 0) return
        val drawSize = if (iconSize.isUnspecified || iconSize.isEmpty()) {
            painter.intrinsicSize
        } else {
            iconSize
        }
        with(painter) {
            val size = size

            if (size.isUnspecified || size.isEmpty()) {
                draw(drawSize, alpha)
            } else {
                inset(
                    horizontal = (size.width - drawSize.width) / 2,
                    vertical = (size.height - drawSize.height) / 2,
                ) {
                    draw(drawSize, alpha)
                }
            }
        }
    }
}

@Composable
internal fun coverPlaceholderPainter(
    iconSize: Dp = Dp.Unspecified,
): Painter = CoverPlaceholderPainter(
    background = MaterialTheme.colorScheme.surfaceContainer,
    foreground = painterResource(org.koitharu.toadlink.ui.R.drawable.ic_media),
    iconSize = if (iconSize.isUnspecified) {
        Size.Unspecified
    } else {
        with(LocalDensity.current) {
            with(density) {
                Size(
                    width = iconSize.toPx(),
                    height = iconSize.toPx()
                )
            }
        }
    },
)