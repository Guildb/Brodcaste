package com.example.servicesmaps

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat


class MapService: Service(), LocationListener {

    lateinit var receiver: BroadcastReceiver
    lateinit var mgr:LocationManager
    var currentLoc = LatLon(0.0 , 0.0)


    inner class MapServiceBinder(val mapService: MapService): android.os.Binder()

    // start handler
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this,"Permission not granted", Toast.LENGTH_LONG).show()
        receiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action){

                    "startGps" -> startGps()
                    "stopGps" -> stopGps()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("startGps")
            addAction("stopGps")
        }

        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        startGps()
        return START_STICKY // we will look at this return value below
    }

    // bind handler - not needed in many cases but defined as an abstract
    // method in Service, therefore must be overridden
    override fun onBind(intent: Intent?): IBinder {
        return MapServiceBinder(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGps()
    }

    @SuppressLint("MissingPermission")
    fun startGps(){
        mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this)
    }

    fun stopGps(){
        mgr.removeUpdates(this)

    }

    override fun onLocationChanged(loc: Location) {
        val broscast = Intent().apply{
            action = "sendLocation"
            putExtra("lat", loc.latitude )
            putExtra("lon", loc.longitude)
        }
        sendBroadcast(broscast)

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

}