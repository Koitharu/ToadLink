package org.koitharu.toadlink.ui.nav

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionCheck(permission: String): State<Boolean> {
    val context = LocalContext.current
    val isGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(RequestPermission()) { result ->
        isGranted.value = result
    }
    LaunchedEffect(Unit) {
        if (!isGranted.value) {
            launcher.launch(permission)
        }
    }
    return isGranted
}