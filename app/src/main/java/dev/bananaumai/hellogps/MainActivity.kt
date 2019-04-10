package dev.bananaumai.hellogps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSettingsClient: SettingsClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSettingsClient = LocationServices.getSettingsClient(this)
        locationCallback = createLocationCallback()

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationTracking()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking()
            }
        }
    }

    private fun startLocationTracking() {
        lastLocation()
        val builder = LocationSettingsRequest.Builder()
        val request = createLocationRequest()
        //builder.addLocationRequest(request)

        locationSettingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener(this) {
                try {
                    fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.myLooper())
                } catch (e: SecurityException) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
                    Log.e("bananaumai", e.message)
                }

            }
            .addOnFailureListener(this) {
                throw it
            }
    }

    private fun stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationCallback() = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations){
                Log.d("bananaumai", "$location")
            }
        }
    }


    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create() ?: throw RuntimeException("couldn't create locatilon request")

        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        return locationRequest
    }

    private fun lastLocation() {

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.d("bananaumai", "$location")
            }
        } catch (e: SecurityException) {
            Log.e("bananaumai", e.message)
        }
    }
}
