package com.asik.hmsgmssupportexample2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.dsl.extension.requestPermissions

const val PERMISSION_ACCESS_LOCATION = 2300

fun AppCompatActivity.requestLocationPermission(
    forceRequest: Boolean = false,
    actionWhenGranted: () -> Unit
) {
    requestPermissions(
        Manifest.permission.ACCESS_FINE_LOCATION
    ) {
        val activity = this@requestLocationPermission
        requestCode = PERMISSION_ACCESS_LOCATION
        resultCallback = {
            when (this) {
                is PermissionResult.PermissionGranted -> {
                    actionWhenGranted.invoke()
                    // start location update if activity is LocationStartListener
                    if (forceRequest) {
                        (activity as? LocationStartListener)?.forceTurnOnLocationService()
                    } else {
                        (activity as? LocationStartListener)?.startLocationUpdate()
                    }
                }
                is PermissionResult.PermissionDenied -> {
                    if (forceRequest) {
                        activity.requestLocationPermission(forceRequest, actionWhenGranted)
                    }
                }
                is PermissionResult.PermissionDeniedPermanently -> {
                    if (forceRequest) {
                        activity.requestLocationPermission(forceRequest, actionWhenGranted)
                    } else {
                        activity.openApplicationSettings()
                    }
                }
                is PermissionResult.ShowRational -> {
                    // show some rational view with reason why you need location access
                }
            }
        }
    }
}

fun Activity.openApplicationSettings() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val scheme = "package"
    val uri = Uri.fromParts(scheme, packageName, null)
    intent.data = uri
    startActivity(intent)
}
