package org.koitharu.toadlink.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
fun BackNavigationIcon() {
    val router = LocalRouter.current
    IconButtonWithTooltip(
        tooltip = stringResource(R.string.back),
        onClick = { router.back() }
    ) {
        Icon(
            painterResource(id = R.drawable.ic_back),
            contentDescription = stringResource(R.string.back)
        )
    }
}