package com.asik.hmsgmssupportexample2

interface LocationStartListener {
    val isGpsProviderEnabled: Boolean

    fun startLocationUpdate()
    fun forceTurnOnLocationService()
}