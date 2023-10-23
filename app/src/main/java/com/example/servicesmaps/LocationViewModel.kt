package com.example.servicesmaps


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// You'll find these imports useful for the network communication and JSON parsing

data class LatLon(var lat: Double=0.0, var lon: Double=0.0)

class LocationViewModel : ViewModel(){
    // Create a latLon property (of type LatLon) and corresponding LiveData, as last week
    var location = LatLon(0.0, 0.0)
    val locationLive = MutableLiveData<LatLon>()

    fun updateGeo(newLoc:LatLon){
        location = newLoc
        locationLive.value = location
    }
}