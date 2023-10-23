package com.example.servicesmaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {

    var service:MapService? = null
    val viewModel: LocationViewModel by viewModels()
    var permissionsGranted = false
    lateinit var receiver: BroadcastReceiver
    lateinit var serviceConn: ServiceConnection


    // Add your service as an attribute of the main activity (nullable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Configuration.getInstance()
            .load(this, PreferenceManager.getDefaultSharedPreferences(this))

        val map1 : MapView = findViewById(R.id.map1)
        map1.controller?.setZoom(14.0)
        viewModel.locationLive.observe( this, Observer {
            map1.controller?.setCenter(GeoPoint(it.lat, it.lon))
        })
        requestPermissions()

        findViewById<Button>(R.id.btnStartGps).setOnClickListener{
            if(permissionsGranted){
                val broscast = Intent().apply{
                    action = "startGps"
                }
                sendBroadcast(broscast)
            }else{
                Toast.makeText(this,"Permission not granted", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.btnGetGps).setOnClickListener{
            if(permissionsGranted){
                service?.apply{
                    viewModel.updateGeo(this.currentLoc)
                }
            }else{
                Toast.makeText(this,"Permission not granted",Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.btnStopGps).setOnClickListener{
            if(permissionsGranted){
                val broscast = Intent().apply{
                    action = "stopGps"
                }
                sendBroadcast(broscast)

            }else{
                Toast.makeText(this,"Permission not granted",Toast.LENGTH_LONG).show()
            }
        }

        receiver = object:BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action){

                    "sendLocation" -> intent.apply {
                        viewModel.updateGeo(LatLon(this.getDoubleExtra("lat",0.0),this.getDoubleExtra("lon",0.0)))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("sendLocation")
        }

        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            permissionsGranted = true
            //initService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true
            initService()
        } else {
            AlertDialog.Builder(this).setPositiveButton("OK", null).setMessage("GPS permission denied").show()
        }
    }

    fun initService() {
        // Start and bind the service here...
        val startIntent = Intent(this, MapService::class.java)
        startService(startIntent)

        serviceConn = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as MapService.MapServiceBinder).mapService
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }

        val bindIntent = Intent(this, MapService::class.java);
        bindService(bindIntent, serviceConn,  Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConn)
        unregisterReceiver(receiver)
    }




}