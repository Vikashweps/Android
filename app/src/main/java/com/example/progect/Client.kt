package com.example.progect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    private lateinit var btnClient: Button
    private lateinit var btnSaveData: Button
    private lateinit var btnExit: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private val dataRecords = ArrayList<JSONObject>()
    private val dataFile = "collected_data.json"

    private var isRecording = false
    private var isClientRunning = false

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (hasPermissions() && isLocationEnabled()) {
            startDataCollection()
        } else {
            requestPermissions()
        }
    }

    private fun initViews() {
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

        btnClient = findViewById(R.id.client)
        btnSaveData = findViewById(R.id.btnSaveData)
        btnExit = findViewById(R.id.button11)

        btnExit.setOnClickListener { finish() }
        btnSaveData.setOnClickListener { toggleRecording() }
        btnClient.setOnClickListener { toggleClient() }

        updateButtons()
        resetNetworkViews()
        loadSavedData()
    }

    private fun updateButtons() {
        btnSaveData.text = if (isRecording) "Стоп запись" else "Старт запись"
        btnClient.text = if (isClientRunning) "Стоп клиент" else "Старт клиент"
    }

    private fun toggleClient() {
        if (isClientRunning) {
            stopClient()
        } else {
            startClient()
        }
        updateButtons()
    }

    private fun saveCurrentData() {
        try {
            val record = JSONObject().apply {
                put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                put("location", JSONObject().apply {
                    put("latitude", tvLatitude.text.toString().trim())
                    put("longitude", tvLongitude.text.toString().trim())
                })
                put("network", JSONObject().apply {
                    put("type", tvNetworkType.text.toString().trim())
                    put("operator", tvOperator.text.toString().trim())
                    put("tac_lac", tvTacLac.text.toString().trim())
                    put("pci", tvPci.text.toString().trim())
                    put("ci", tvCid.text.toString().trim())
                    put("RSRP", tvSignal.text.toString().trim())
                })
            }
            dataRecords.add(record)
            saveToFile()
            setStatus("Записей: ${dataRecords.size}")
        } catch (e: Exception) {
            // Игнорируем ошибки
        }
    }

    private fun toggleRecording() {
        isRecording = !isRecording
        updateButtons()

        if (isRecording) {
            showToast("Запись запущена")
            saveCurrentData() // первая запись сразу
        } else {
            showToast("Запись остановлена")
            setStatus("Всего записей: ${dataRecords.size}")
        }
    }
    private fun saveToFile() {
        try {
            File(filesDir, dataFile).writeText(JSONArray(dataRecords).toString())
        } catch (e: Exception) {
        }
    }

    private fun loadSavedData() {
        try {
            val file = File(filesDir, dataFile)
            if (file.exists()) {
                val text = file.readText()
                if (text.isNotBlank()) {
                    val array = JSONArray(text)
                    dataRecords.clear()
                    for (i in 0 until array.length()) {
                        dataRecords.add(array.getJSONObject(i))
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun readDataAsString(): String {
        return try {
            val file = File(filesDir, dataFile)
            if (file.exists()) file.readText() else "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun startDataCollection() {
        startLocationUpdates()
        startCellUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasPermissions()) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { updateLocationUI(it) }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { updateLocationUI(it) }
        }
    }

    private fun updateLocationUI(location: Location) {
        tvLatitude.text = "%.6f".format(location.latitude)
        tvLongitude.text = "%.6f".format(location.longitude)
        tvAltitude.text = "%.1f м".format(location.altitude)
        tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
    }

    @SuppressLint("MissingPermission")
    private fun startCellUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                loadCellInfo()
                if (isRecording) saveCurrentTimeData()
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        })
    }

    private fun saveCurrentTimeData() {
        tvTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
        saveCurrentData()
    }

    @SuppressLint("MissingPermission")
    private fun loadCellInfo() {
        if (!hasPermissions()) {
            resetNetworkViews()
            return
        }

        try {
            val telephony = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cell = telephony.allCellInfo?.find { it.isRegistered }

            if (cell != null) {
                updateNetworkUI(cell)
            } else {
                resetNetworkViews()
            }
        } catch (e: Exception) {
            resetNetworkViews()
        }
    }

    private fun updateNetworkUI(cell: CellInfo) {
        when (cell) {
            is CellInfoGsm -> showCellInfo("2G GSM", cell.cellIdentity, cell.cellSignalStrength.dbm)
            is CellInfoLte -> showCellInfo("4G LTE", cell.cellIdentity, getRsrp(cell.cellSignalStrength))
            is CellInfoWcdma -> showCellInfo("3G WCDMA", cell.cellIdentity, cell.cellSignalStrength.dbm)
            else -> resetNetworkViews()
        }
    }

    private fun getRsrp(strength: CellSignalStrength): Int {
        return if (strength is CellSignalStrengthLte) {
            strength.rsrp
        } else {
            CellInfo.UNAVAILABLE
        }
    }

    private fun showCellInfo(type: String, id: CellIdentity, signal: Int) {
        tvNetworkType.text = type
        tvOperator.text = getOperator(id)
        tvTacLac.text = getTacLac(id).takeIf { it != CellInfo.UNAVAILABLE }?.toString() ?: "-"
        tvPci.text = getPci(id).takeIf { it != CellInfo.UNAVAILABLE }?.toString() ?: "-"
        tvCid.text = getCid(id).takeIf { it != CellInfo.UNAVAILABLE }?.toString() ?: "-"
        tvSignal.text = if (signal == CellInfo.UNAVAILABLE) "-" else "$signal dBm"
    }

    private fun getOperator(id: CellIdentity): String {
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
        return "${mcc ?: "-"}/${mnc ?: "-"}"
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
        tvNetworkType.text = "-"
        tvOperator.text = "-"
        tvTacLac.text = "-"
        tvPci.text = "-"
        tvCid.text = "-"
        tvSignal.text = "-"
    }

    private fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ), PERMISSION_REQUEST_CODE)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setStatus(msg: String) {
        tvStatus.text = msg
    }

    private fun startClient() {
        isClientRunning = true
        Thread {
            try {
                ZContext().use { ctx ->
                    ctx.createSocket(SocketType.REQ).use { sock ->
                        sock.connect("tcp://$SERVER_IP:$SERVER_PORT")
                        sock.sendTimeOut = 5000
                        sock.receiveTimeOut = 10000

                        setStatus("Отправка данных...")

                        val data = readDataAsString()
                        if (data == "[]" || data.isEmpty()) {
                            setStatus("Нет данных")
                            return@Thread
                        }

                        sock.send(data.toByteArray(), 0)
                        val reply = sock.recv(0)

                        if (reply != null) {
                            val resp = String(reply)
                            if (resp.contains("OK") || resp.contains("успешно") || resp.contains("запис")) {
                                dataRecords.clear()
                                saveToFile()
                                setStatus("Отправлено и очищено")
                            } else {
                                setStatus("Ошибка сервера")
                            }
                        } else {
                            setStatus("Нет ответа")
                        }
                    }
                }
            } catch (e: Exception) {
                setStatus("Ошибка отправки")
            } finally {
                isClientRunning = false
                runOnUiThread { updateButtons() }
            }
        }.start()
    }

    private fun stopClient() {
        isClientRunning = false
        setStatus("Клиент остановлен")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDataCollection()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissions()) {
            startDataCollection()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}