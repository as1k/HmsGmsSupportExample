package com.asik.hmsgmssupportexample2

import com.asik.hmsgmssupportexample2.model.LocationInfoModel
import java.util.concurrent.ConcurrentLinkedDeque

interface LocationCallbackListener {
    fun onLocationResult(locationsDeque: ConcurrentLinkedDeque<LocationInfoModel>)
    fun onLocationRequest()
    fun onLocationFailed()
}