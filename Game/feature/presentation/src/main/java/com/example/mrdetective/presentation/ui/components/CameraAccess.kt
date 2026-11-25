package com.example.mrdetective.presentation.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Holds the camera capability and permission state along with actions for requesting access and opening system
 * settings.
 */
internal class CameraAccess(
    val available: Boolean,
    val granted: Boolean,
    val showRationale: Boolean,
    val requestOrOpenSettings: (Boolean) -> Unit,
    val openSettings: (Boolean) -> Unit,
)

@Composable
internal fun rememberCameraAccess(onPermissionChoice: (Boolean) -> Unit): CameraAccess {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val available = remember(context) { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) }

    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale() =
        activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } == true

    var granted by remember(context) { mutableStateOf(hasPermission()) }
    var rationale by remember(activity) { mutableStateOf(shouldShowRationale()) }
    var enableAfterSettings by rememberSaveable { mutableStateOf(false) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            granted = it
            rationale = shouldShowRationale()
            onPermissionChoice(it)
        }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = hasPermission()
        rationale = shouldShowRationale()
        val shouldEnable = enableAfterSettings && granted
        enableAfterSettings = false
        if (shouldEnable) onPermissionChoice(true)
    }

    fun openSettings(enableWhenGranted: Boolean) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        try {
            enableAfterSettings = enableWhenGranted
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            enableAfterSettings = false
        }
    }

    return CameraAccess(
        available,
        granted,
        rationale,
        requestOrOpenSettings = { configured ->
            if (hasPermission()) {
                onPermissionChoice(true)
            } else if (configured && !shouldShowRationale()) {
                openSettings(true)
            } else {
                permission.launch(Manifest.permission.CAMERA)
            }
        },
        openSettings = ::openSettings,
    )
}
