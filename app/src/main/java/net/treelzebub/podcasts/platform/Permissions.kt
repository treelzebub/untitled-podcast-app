package net.treelzebub.podcasts.platform

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


/**
*  Throws [SecurityException] if permission is denied.
*/
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermission() {
    var hasGrantedPermission by remember { mutableStateOf(false) }
    val permissionResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasGrantedPermission = it }
    )

    LaunchedEffect("notif-permission") {
        permissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
