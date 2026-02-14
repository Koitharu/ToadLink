package org.koitharu.toadlink.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.koitharu.toadlink.settings.preferences.PreferenceCategory
import org.koitharu.toadlink.settings.screens.fileManagerPreferenceScreen
import org.koitharu.toadlink.ui.R
import org.koitharu.toadlink.ui.composables.BackNavigationIcon
import org.koitharu.toadlink.ui.mvi.MviIntentHandler
import org.koitharu.toadlink.ui.nav.LocalRouter

@Composable
fun SettingsScreen() {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val state by viewModel.collectState()
    val snackbarHostState = remember { SnackbarHostState() }
    LocalRouter.current
    LocalContext.current
//    LaunchedEffect("errors") {
//        viewModel.effect.collect { effect ->
//            when (effect) {
//                is OnError -> snackbarHostState.showSnackbar(effect.error.getDisplayMessage(context))
//                is FindDeviceEffect.OpenDevice -> router.changeRoot(
//                    ControlDestination(effect.deviceId)
//                )
//            }
//        }
//    }
    SettingsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        handleIntent = viewModel,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    handleIntent: MviIntentHandler<SettingsIntent>,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                navigationIcon = {
                    BackNavigationIcon()
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { contentPadding ->
            LazyColumn(
                contentPadding = contentPadding
            ) {
                item {
                    PreferenceCategory(stringResource(R.string.files))
                }
                fileManagerPreferenceScreen(
                    state = state,
                    handleIntent = handleIntent,
                )
            }
        }
    )
}


@Composable
@Preview
private fun PreviewSettingsContent() = MaterialTheme {
    SettingsContent(
        state = SettingsState(),
        snackbarHostState = SnackbarHostState(),
        handleIntent = MviIntentHandler.NoOp,
    )
}