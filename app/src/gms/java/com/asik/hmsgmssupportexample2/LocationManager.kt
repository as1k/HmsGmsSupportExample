package com.asik.hmsgmssupportexample2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.asik.hmsgmssupportexample2.model.LocationInfoModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.location.LocationSettingsRequest
import java.util.concurrent.ConcurrentLinkedDeque

const val LOCATION_SETTING_RETRY_REQUEST_CODE = 2301
typealias AndroidLocationService = android.location.LocationManager

class LocationManager constructor(
    private val context: Context,
    private val locationCallbackListener: LocationCallbackListener
) : LocationCallback() {

    companion object {
        const val DEFAULT_UPDATE_INTERVAL_IN_MILLISECONDS = 500L
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.create()

    private val settingsClient: SettingsClient
    private val locationSettingsRequest: LocationSettingsRequest

    // deque that collects latest new 100 locations
    private val locationsDeque: ConcurrentLinkedDeque<LocationInfoModel> = ConcurrentLinkedDeque()

    val isGpsProviderEnabled: Boolean
        get() {
            val locationService =
                context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationService
            return locationService.isProviderEnabled(AndroidLocationService.GPS_PROVIDER)
        }

    var isLocationUpdateRunning = false

    init {
        locationRequest.interval = DEFAULT_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = DEFAULT_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        settingsClient = LocationServices.getSettingsClient(context)

        val requestBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        requestBuilder.setAlwaysShow(true)
        locationSettingsRequest = requestBuilder.build()
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        locationResult?.let { locationResultData ->
            val location = locationResultData.lastLocation
            val locationInfoModel = LocationInfoModel(
                location.longitude,
                location.latitude,
                location.accuracy.toInt()
            )

            locationsDeque.addWithLimitCheck(locationInfoModel)
            locationCallbackListener.onLocationResult(locationsDeque)
        }
    }

    override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
        locationAvailability?.let {
            val isLocationAvailable = it.isLocationAvailable
            if (!isLocationAvailable) {
                locationCallbackListener.onLocationFailed()
                isLocationUpdateRunning = false
            }
        }
    }

    fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationCallbackListener.onLocationFailed()
            isLocationUpdateRunning = false
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            this,
            Looper.getMainLooper()
        )
        locationCallbackListener.onLocationRequest()
        isLocationUpdateRunning = isGpsProviderEnabled
    }

    fun forceTurnOnLocationService() {
        if (isGpsProviderEnabled) {
            requestLocationUpdates()
            return
        }
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener { response ->
                val states = response.locationSettingsStates ?: return@addOnSuccessListener
                if (states.isLocationPresent) {
                    requestLocationUpdates()
                }
            }
            .addOnFailureListener { exception ->
                justTry {
                    (exception as? ResolvableApiException)?.startResolutionForResult(
                        context as Activity,
                        LOCATION_SETTING_RETRY_REQUEST_CODE
                    )
                }
            }
    }

    fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(this)
        isLocationUpdateRunning = false
    }
}
