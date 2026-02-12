package org.koitharu.toadlink.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
fun BackNavigationIcon() = TooltipBox(
    positionProvider =
        TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
    tooltip = { PlainTooltip { Text(stringResource(R.string.back)) } },
    state = rememberTooltipState(),
) {
    val router = LocalRouter.current
    IconButton(
        onClick = { router.back() }
    ) {
        Icon(
            painterResource(id = R.drawable.ic_back),
            contentDescription = stringResource(R.string.back)
        )
    }
}