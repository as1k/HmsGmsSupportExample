package com.asik.hmsgmssupportexample2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.asik.hmsgmssupportexample2.model.LocationInfoModel
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ConcurrentLinkedDeque

class MainActivity : AppCompatActivity(), LocationCallbackListener, LocationStartListener {

    private var buttonGetUserLocation: MaterialButton? = null

    private var locationManager: LocationManager? = null
    override val isGpsProviderEnabled: Boolean
        get() = locationManager?.isGpsProviderEnabled ?: false

    override fun startLocationUpdate() {
        if (locationManager?.isLocationUpdateRunning == false) {
            locationManager?.requestLocationUpdates()
        }
    }

    override fun forceTurnOnLocationService() {
        if (locationManager?.isLocationUpdateRunning == false) {
            locationManager?.forceTurnOnLocationService()
        }
    }

    override fun onLocationResult(locationsDeque: ConcurrentLinkedDeque<LocationInfoModel>) {
        val location = locationsDeque.last
        showSuccessToast("New location with latitude:${location.latitude},\nlongitude:${location.longitude},\naccuracy:${location.accuracy} found")
    }

    override fun onLocationRequest() {
        // do nothing
    }

    override fun onLocationFailed() {
        // do nothing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLocationManager()
        bindViews()
    }

    override fun onDestroy() {
        locationManager?.removeLocationUpdates()
        super.onDestroy()
    }

    private fun initLocationManager() {
        locationManager = LocationManager(context = this, locationCallbackListener = this)
    }

    private fun bindViews() {
        buttonGetUserLocation = findViewById(R.id.button_get_user_location)
        buttonGetUserLocation?.setOnClickListener {
            requestLocationPermission(
                forceRequest = true,
                actionWhenGranted = { showSuccessToast("User location search started") })
        }
    }

    private fun showSuccessToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
