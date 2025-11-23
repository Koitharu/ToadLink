package org.koitharu.toadlink.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLayoutDirection

@ReadOnlyComposable
@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val ld = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(ld) + other.calculateStartPadding(ld),
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        end = this.calculateEndPadding(ld) + other.calculateEndPadding(ld),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
    )
}