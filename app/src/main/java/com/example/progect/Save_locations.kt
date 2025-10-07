package com.example.progect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.text.SimpleDateFormat
import java.util.Locale


class Save_locations : AppCompatActivity() {

    private var log_tag: String = "MY_LOG_TAG"

    private lateinit var btnStartServer: Button
    private lateinit var btnStartClient: Button

    private lateinit var tvStatus: TextView
    private lateinit var handler: Handler

    private var clientThread: Thread? = null
    private var serverThread: Thread? = null
    private var isClientRunning = false
    private var isServerRunning = false

    private lateinit var btnBack: Button

    private lateinit var btnEnableLocation: Button
    private lateinit var tvLatitudeValue: TextView
    private lateinit var tvLongitudeValue: TextView
    private lateinit var tvAltitudeValue: TextView
    private lateinit var tvTimeValue: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { updateUI(it) }
        }
    }
    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(1000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateUI(location)
                } ?: run {
                    Log.w(log_tag, "onLocationResult: lastLocation is null")
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

        tvLatitudeValue.text = " ${"%.6f".format(location.latitude)}"
        tvLongitudeValue.text = " ${"%.6f".format(location.longitude)}"
        tvAltitudeValue.text = " ${"%.1f".format(location.altitude)} м"
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
        tvTimeValue.text = "$currentTime"
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
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
        Log.d(log_tag, "Запрашиваем разрешения на геолокацию")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION  )
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


    private fun initViews() {
        handler = Handler(Looper.getMainLooper())
        tvStatus = findViewById(R.id.textView)

        btnStartServer = findViewById(R.id.server)
        btnStartClient = findViewById(R.id.client)
        btnBack = findViewById(R.id.button11)
        btnEnableLocation = findViewById(R.id.button12)

        tvLatitudeValue = findViewById(R.id.tvLatitud)
        tvLongitudeValue = findViewById(R.id.tvLongitude)
        tvAltitudeValue = findViewById(R.id.tvAltitude)
        tvTimeValue = findViewById(R.id.tvTime)

        btnEnableLocation.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        tvLatitudeValue.text = "-"
        tvLongitudeValue.text = "-"
        tvAltitudeValue.text = "-"
        tvTimeValue.text = "-"
        tvStatus.text = "Отключено"
    }

    private fun setupButtonListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnStartServer.setOnClickListener {
            if (!isServerRunning) {
                Log.d(log_tag, "Starting server...")
                startServer()
                btnStartServer.text = "Стоп сервер"
            } else {
                Log.d(log_tag, "Stopping server...")
                stopServer()
                btnStartServer.text = "Старт сервер"
            }
        }

        btnStartClient.setOnClickListener {
            if (!isClientRunning) {
                Log.d(log_tag, "Starting client...")
                startClient()
                btnStartClient.text = "Стоп клиент"
            } else {
                Log.d(log_tag, "Stopping client...")
                stopClient()
                btnStartClient.text = "Старт клиент"
            }
        }
    }

    fun startServer() {
        isServerRunning = true
        serverThread = Thread {
            Log.d(log_tag, "[SERVER THREAD] Server thread started")
            try {
                val context = ZContext()
                val socket = context.createSocket(SocketType.REP)
                socket.bind("tcp://0.0.0.0:5555")
                Log.d(log_tag, "[SERVER] Bound to tcp://0.0.0.0:5555")

                var counter = 0

                while (isServerRunning) {
                    try {
                        val requestBytes = socket.recv(ZMQ.NOBLOCK)
                        if (requestBytes != null) {
                            val request = String(requestBytes, ZMQ.CHARSET)
                            counter++
                            Log.d(log_tag, "[SERVER] Received: $request (total: $counter)")

                            handler.post {
                                tvStatus.text = "Получено: $request\n Счётчик: $counter"
                            }

                            val response = "Hello from Android Server! Count: $counter"
                            socket.send(response.toByteArray(ZMQ.CHARSET), 0)
                            Log.d(log_tag, "[SERVER] Sent response: $response")
                        }

                        Thread.sleep(100)
                    } catch (e: Exception) {
                        Log.e(log_tag, "[SERVER] Error in loop: ${e.message}")
                    }
                }

                socket.close()
                context.close()
                Log.d(log_tag, "[SERVER] Socket and context closed")
            } catch (e: Exception) {
                Log.e(log_tag, "[SERVER] Setup error: ${e.message}")
            } finally {
                Log.d(log_tag, "[SERVER THREAD] Server thread finished")
            }
        }
        serverThread?.start()
    }

    private fun stopServer() {
        Log.d(log_tag, "stopServer() called")
        isServerRunning = false
        serverThread?.interrupt()
        runOnUiThread {
            tvStatus.text = "Сервер остановлен"
            btnStartServer.text = "Старт сервер"
        }
    }

    private fun startClient() {
        isClientRunning = true
        clientThread = Thread {
            Log.d(log_tag, "[CLIENT THREAD] Client thread started")
            val context = ZContext()
            val socket = context.createSocket(SocketType.REQ)

            try {
                val serverAddress = "tcp://172.20.10.3:5557"
                socket.connect(serverAddress)
                Log.d(log_tag, "[CLIENT] Connected to $socket.connect(serverAddress)")

                runOnUiThread {
                    tvStatus.text = "Подключено к $serverAddress\n Ожидаю ответа..."
                }

                var messageCount = 0
                while (isClientRunning && messageCount < 100 && !Thread.currentThread().isInterrupted) {
                    try {
                        val lat = tvLatitudeValue.text.toString().trim()
                        val lon = tvLongitudeValue.text.toString().trim()
                        val alt = tvAltitudeValue.text.toString().trim()
                        val time = tvTimeValue.text.toString().trim()

                        val message = " $lat, $lon, $alt, $time"
                        ++messageCount
                        socket.send(message.toByteArray(ZMQ.CHARSET), 0)
                        Log.d(log_tag, "[CLIENT] Sent: $message")

                        val reply = socket.recv(0)
                        if (reply != null) {
                            val response = String(reply, ZMQ.CHARSET)
                            Log.d(log_tag, "[CLIENT] Received: $response")

                            runOnUiThread {
                                tvStatus.text = "Отправлено: $messageCount - $message\nПолучено: $response\n "
                            }
                        } else {
                            Log.e(log_tag, "[CLIENT] No reply from server")
                            runOnUiThread {
                                tvStatus.text = "Нет ответа от сервера\nПопытка: $messageCount"
                            }
                        }
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        Log.d(log_tag, "[CLIENT] Thread interrupted")
                        break
                    } catch (e: Exception) {
                        Log.e(log_tag, "[CLIENT] Send/receive error: ${e.message}")
                        runOnUiThread {
                            tvStatus.text = "Ошибка: ${e.message}\n"
                        }
                        Thread.sleep(3000)
                    }
                }
            } catch (e: Exception) {
                Log.e(log_tag, "[CLIENT] Setup error: ${e.message}")
                runOnUiThread {
                    tvStatus.text = "Не удалось подключиться: ${e.message}"
                    btnStartClient.text = "Start Client"
                }
            } finally {
                try {
                    socket.close()
                    context.close()
                    Log.d(log_tag, "[CLIENT] Socket and context closed")
                } catch (e: Exception) {
                    Log.e(log_tag, "[CLIENT] Error closing resources: ${e.message}")
                }
                isClientRunning = false
                runOnUiThread {
                    btnStartClient.text = "Start Client"
                    tvStatus.append("\nКлиент остановлен")
                }
                Log.d(log_tag, "[CLIENT THREAD] Client thread finished")
            }
        }
        clientThread?.start()
    }

    private fun stopClient() {
        Log.d(log_tag, "stopClient() called")
        isClientRunning = false
        clientThread?.interrupt()
        runOnUiThread {
            tvStatus.text = "Клиент остановлен"
            btnStartClient.text = "Старт клиент"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(log_tag, "SaveLocations onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_locations)

        initViews()
        setupButtonListeners()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
        setupBackButton()

        if (checkPermissions() && isLocationEnabled()) {
            getLastKnownLocation()
            startLocationUpdates()
        }
        else {
            requestPermissions() //
        }
    }
    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastKnownLocation()
            startLocationUpdates()
        } else {
            requestPermissions()
        }
    }override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(log_tag, "onDestroy: stopping server and client")
        stopClient()
        stopServer()
        stopLocationUpdates()
    }
}