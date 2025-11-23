package org.koitharu.toadlink.ui.util

import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.content.res.getDimensionPixelOffsetOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow

@Composable
@ReadOnlyComposable
@Px
fun themeAttributeSize(
    @AttrRes resId: Int,
    @Px fallback: Dp = Dp.Unspecified,
): Dp {
    val context = LocalContext.current
    return context.obtainStyledAttributes(intArrayOf(resId)).use {
        if (it.hasValue(0)) {
            it.getDimensionPixelSizeOrThrow(0).pxToDp()
        } else {
            fallback
        }
    }
}

@Composable
@ReadOnlyComposable
fun themeAttributeOffset(
    @AttrRes resId: Int,
    @Px fallback: Dp = Dp.Unspecified,
): Dp {
    val context = LocalContext.current
    return context.obtainStyledAttributes(intArrayOf(resId)).use {
        if (it.hasValue(0)) {
            it.getDimensionPixelOffsetOrThrow(0).pxToDp()
        } else {
            fallback
        }
    }
}

@Composable
@ReadOnlyComposable
private fun Int.pxToDp(): Dp {
    val density = LocalDensity.current
    return with(density) {
        this@pxToDp.toDp()
    }
}