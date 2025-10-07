package com.example.progect
import java.io.File
import java.io.FileWriter
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Locale

class Locations : AppCompatActivity() {

    private lateinit var goToBack: Button
    private lateinit var location: Button
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var tvTime: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val LOG_TAG = "LOCATION_ACTIVITY"
    }


    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { updateUI(it) }
        }
    }



    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateUI(location)
                } ?: run {
                    Log.w(LOG_TAG, "onLocationResult: lastLocation is null")
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Геолокация отключена", Toast.LENGTH_SHORT).show()
            location.visibility = View.VISIBLE
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateUI(location: Location) {

        tvLatitude.text = " ${"%.6f".format(location.latitude)}"
        tvLongitude.text = " ${"%.6f".format(location.longitude)}"
        tvAltitude.text = " ${"%.1f".format(location.altitude)} м"
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
        tvTime.text = "Now: $currentTime"
    }

    private fun setupBackButton() {
        goToBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        Log.d(LOG_TAG, "Запрашиваем разрешения на геолокацию")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Требуется разрешение для работы", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MY_LOG_TAG", "Locations onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)
        enableEdgeToEdge()

        goToBack = findViewById(R.id.Back)
        tvLatitude = findViewById(R.id.textView5)
        tvLongitude = findViewById(R.id.textView6)
        tvAltitude = findViewById(R.id.textView7)
        tvTime = findViewById(R.id.textView8)
        location = findViewById<Button>(R.id.location)

        location.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
        setupBackButton()
    }
    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastKnownLocation()
            startLocationUpdates()
        } else {
            requestPermissions()
        }

    }

    override fun onStop() {
        super.onStop()
        Log.d("MY_LOG_TAG", "onStop method")
        stopLocationUpdates()
    }
}
