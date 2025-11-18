package com.example.progect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Client : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvNetworkType: TextView
    private lateinit var tvOperator: TextView
    private lateinit var tvTacLac: TextView
    private lateinit var tvPci: TextView
    private lateinit var tvCid: TextView
    private lateinit var tvSignal: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var handler: Handler
    private val dataRecords = ArrayList<JSONObject>()
    private val dataFile = "collected_data.json"

    private var isRecording = false
    private var isClientRunning = false
    private var isServerRunning = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val UPDATE_INTERVAL = 10000L
        private const val SERVER_IP = "172.20.10.3"
        private const val SERVER_PORT = "5557"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        initViews()
        loadSavedData()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (hasAllPermissions() && isLocationEnabled()) {
            startDataUpdates()
        } else {
            requestPermissions()
        }
    }

    private fun initViews() {
        handler = Handler(Looper.getMainLooper())

        // Находим все View
        tvStatus = findViewById(R.id.tvStatus)
        tvLatitude = findViewById(R.id.tvLatitud)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvAltitude = findViewById(R.id.tvAltitude)
        tvTime = findViewById(R.id.tvTime)
        tvNetworkType = findViewById(R.id.tvNetworkType)
        tvOperator = findViewById(R.id.tvOperator)
        tvTacLac = findViewById(R.id.tvTacLac)
        tvPci = findViewById(R.id.tvPci)
        tvCid = findViewById(R.id.tvCid)
        tvSignal = findViewById(R.id.tvSignal)

        // Настройка кнопок
        setupButtons()
        resetNetworkViews()
    }

    private fun setupButtons() {
        val buttons = mapOf(
            R.id.button11 to { finish() },

            R.id.client to { toggleClient() },
            R.id.btnSaveData to { toggleRecording() }
        )

        buttons.forEach { (id, action) ->
            findViewById<Button>(id).setOnClickListener { action() }
        }
    }

    private fun refreshData() {
        if (hasAllPermissions()) {
            getLastKnownLocation()
            loadCellInfo()
        }
    }

    private fun toggleRecording() {
        isRecording = !isRecording
        updateRecordButton()
        showToast(if (isRecording) "Автосохранение запущено" else "Автосохранение остановлено")
        updateStatus("Запись ${if (isRecording) "запущена" else "остановлена"} (${dataRecords.size} записей)")

        if (isRecording) saveCurrentData()
    }


    private fun toggleClient() {
        if (!isClientRunning) startClient() else stopClient()
        updateClientButton()
    }

    private fun updateRecordButton() {
        findViewById<Button>(R.id.btnSaveData).text = if (isRecording) "Стоп запись" else "Старт запись"
    }



    private fun updateClientButton() {
        findViewById<Button>(R.id.client).text = if (isClientRunning) "Стоп клиент" else "Старт клиент"
    }

    // JSON функции
    private fun saveCurrentData() {
        try {
            dataRecords.add(createDataObject())
            saveDataToFile()
            updateStatus("Данные сохранены (${dataRecords.size} записей)")
        } catch (e: Exception) {
            Log.e("APP", "Ошибка сохранения: ${e.message}")
        }
    }

    private fun createDataObject() = JSONObject().apply {
        put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        put("location", JSONObject().apply {
            put("latitude", tvLatitude.text.cleanText())
            put("longitude", tvLongitude.text.cleanText())
        })
        put("network", JSONObject().apply {
            put("type", tvNetworkType.text.cleanText())
            put("operator", tvOperator.text.cleanText())
            put("tac_lac", tvTacLac.text.cleanText())
            put("pci", tvPci.text.cleanText())
            put("ci", tvCid.text.cleanText())
            put("RSRP", tvSignal.text.cleanText())
        })
    }

    private fun CharSequence.cleanText() = toString().trim().removePrefix(" ")

    private fun saveDataToFile() {
        try {
            File(filesDir, dataFile).writeText(JSONArray(dataRecords).toString(4))
        } catch (e: Exception) {
            Log.e("APP", "Ошибка записи файла: ${e.message}")
        }
    }

    private fun loadSavedData() {
        try {
            File(filesDir, dataFile).takeIf { it.exists() }?.readText()?.let { jsonString ->
                JSONArray(jsonString).let { jsonArray ->
                    dataRecords.clear()
                    for (i in 0 until jsonArray.length()) {
                        dataRecords.add(jsonArray.getJSONObject(i))
                    }
                    Log.d("APP", "Загружено ${dataRecords.size} записей")
                }
            }
        } catch (e: Exception) {
            Log.e("APP", "Ошибка загрузки: ${e.message}")
        }
    }

    private fun readDataFromFile() = try {
        File(filesDir, dataFile).takeIf { it.exists() }?.readText() ?: "[]"
    } catch (e: Exception) {
        Log.e("APP", "Ошибка чтения: ${e.message}")
        "[]"
    }

    // Обновления данных
    private fun startDataUpdates() {
        refreshData()
        startLocationUpdates()
        handler.post(updateTask)
    }

    private fun stopDataUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacks(updateTask)
    }

    private val updateTask = object : Runnable {
        override fun run() {
            loadCellInfo()
            if (isRecording) saveCurrentData()
            handler.postDelayed(this, UPDATE_INTERVAL)
        }
    }

    // Геолокация
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { updateLocationUI(it) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasAllPermissions()) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { updateLocationUI(it) }
        }
    }

    private fun updateLocationUI(location: Location) {
        tvLatitude.text = " ${"%.6f".format(location.latitude)}"
        tvLongitude.text = " ${"%.6f".format(location.longitude)}"
        tvAltitude.text = " ${"%.1f".format(location.altitude)} м"
        tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
    }

    // Мобильные сети
    private fun loadCellInfo() {
        if (!hasNetworkPermissions()) return

        try {
            (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                .allCellInfo
                ?.find { it.isRegistered }
                ?.let { updateNetworkUI(it) }
                ?: run { resetNetworkViews() }
        } catch (e: Exception) {
            resetNetworkViews()
        }
    }

    private fun updateNetworkUI(cell: CellInfo) {
        when (cell) {
            is CellInfoGsm -> updateCellUI("2G GSM", cell.cellIdentity, cell.cellSignalStrength.dbm)
            is CellInfoLte -> updateCellUI("4G LTE", cell.cellIdentity, cell.cellSignalStrength.rsrp)
            is CellInfoWcdma -> updateCellUI("3G WCDMA", cell.cellIdentity, cell.cellSignalStrength.dbm)
            else -> { /* Для других типов сетей ничего не делаем */ }
        }
    }

    private fun updateCellUI(type: String, id: CellIdentity, signal: Int) {
        tvNetworkType.text = " $type"
        tvOperator.text = " ${getMccMnc(id)}"
        tvTacLac.text = " ${getTacLac(id).safe()}"
        tvPci.text = " ${getPci(id).safe()}"
        tvCid.text = " ${getCid(id).safe()}"
        tvSignal.text = " ${signal.safeDbm()}"
    }

    private fun getMccMnc(id: CellIdentity): String {
        val mcc = when (id) {
            is CellIdentityGsm -> id.mccString
            is CellIdentityLte -> id.mccString
            is CellIdentityWcdma -> id.mccString
            else -> null
        }
        val mnc = when (id) {
            is CellIdentityGsm -> id.mncString
            is CellIdentityLte -> id.mncString
            is CellIdentityWcdma -> id.mncString
            else -> null
        }
        return "${mcc.safeValue()}/${mnc.safeValue()}"
    }

    private fun getTacLac(id: CellIdentity) = when (id) {
        is CellIdentityGsm -> id.lac
        is CellIdentityLte -> id.tac
        is CellIdentityWcdma -> id.lac
        else -> CellInfo.UNAVAILABLE
    }

    private fun getPci(id: CellIdentity) = when (id) {
        is CellIdentityGsm -> id.bsic
        is CellIdentityLte -> id.pci
        is CellIdentityWcdma -> id.psc
        else -> CellInfo.UNAVAILABLE
    }

    private fun getCid(id: CellIdentity) = when (id) {
        is CellIdentityGsm -> id.cid
        is CellIdentityLte -> id.ci
        is CellIdentityWcdma -> id.cid
        else -> CellInfo.UNAVAILABLE
    }

    private fun resetNetworkViews() {
        tvNetworkType.text = "Тип: -"
        tvOperator.text = "Оператор: -"
        tvTacLac.text = "TAC/LAC: -"
        tvPci.text = "PCI: -"
        tvCid.text = "CI: -"
        tvSignal.text = "Уровень сигнала: -"
    }

    // Вспомогательные функции
    private fun Int.safe() = if (this == Int.MAX_VALUE || this == CellInfo.UNAVAILABLE) "-" else toString()
    private fun Int.safeDbm() = if (this == Int.MAX_VALUE || this == CellInfo.UNAVAILABLE) "-" else "$this dBm"
    private fun String?.safeValue() = if (isNullOrEmpty() || this == "2147483647") "-" else this

    private fun hasAllPermissions() = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun hasNetworkPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), PERMISSION_REQUEST_CODE)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun updateStatus(message: String) {
        handler.post { tvStatus.text = message }
    }

    private fun showToast(message: String) {
        handler.post { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    // ZeroMQ

    private fun startClient() {
        isClientRunning = true
        Thread {
            try {
                ZContext().use { context ->
                    context.createSocket(SocketType.REQ).use { socket ->
                        socket.connect("tcp://$SERVER_IP:$SERVER_PORT")
                        socket.sendTimeOut = 5000
                        socket.receiveTimeOut = 10000

                        updateStatus("Подключение к серверу")

                        val jsonData = readDataFromFile()

                        if (jsonData != "[]") {
                            socket.send(jsonData.toByteArray(), 0)
                            val reply = socket.recv(0)

                            if (reply != null) {
                                val response = String(reply)
                                handler.post {
                                    // АВТООЧИСТКА после успешной отправки
                                    if (response.contains("Успешно") || response.contains("OK") ||
                                        response.contains("получено") || response.contains("записей")) {

                                        val clearedCount = dataRecords.size
                                        dataRecords.clear()
                                        saveDataToFile()

                                        updateStatus("Данные отправлены и очищены\nОтправлено: $clearedCount записей\nОтвет: $response")
                                        Toast.makeText(this@Client, "Данные отправлены ($clearedCount записей)", Toast.LENGTH_SHORT).show()

                                        Log.d("APP", "Автоочистка: отправлено и очищено $clearedCount записей")
                                    } else {
                                        updateStatus("Ошибка сервера\nОтвет: $response")
                                        Toast.makeText(this@Client, "Ошибка сервера", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                handler.post {
                                    updateStatus("Нет ответа от сервера")
                                    Toast.makeText(this@Client, "Нет ответа от сервера", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            handler.post {
                                updateStatus("Нет данных для отправки")
                                Toast.makeText(this@Client, "Нет данных для отправки", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    updateStatus("Ошибка подключения: ${e.message}")
                    Toast.makeText(this@Client, "Ошибка отправки", Toast.LENGTH_SHORT).show()
                }
                Log.e("APP", "Client error: ${e.message}")
            } finally {
                isClientRunning = false
                handler.post { findViewById<Button>(R.id.client).text = "Старт клиент" }
            }
        }.start()
    }

    private fun stopClient() {
        isClientRunning = false
        updateStatus("Клиент остановлен")
    }

    // Жизненный цикл
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDataUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAllPermissions()) startDataUpdates()
    }

    override fun onPause() = stopDataUpdates()
    override fun onDestroy() {
        stopDataUpdates()
        stopClient()
        super.onDestroy()
    }
}