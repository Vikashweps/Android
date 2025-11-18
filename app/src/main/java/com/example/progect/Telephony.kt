package com.example.progect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class Telephony : AppCompatActivity() {

    // Текстовые поля сети
    private lateinit var tvNetworkType: TextView
    private lateinit var tvOperator: TextView
    private lateinit var tvTacLac: TextView
    private lateinit var tvPci: TextView
    private lateinit var tvCid: TextView
    private lateinit var tvSignal: TextView
    private lateinit var tvAsu: TextView
    private lateinit var tvRsrq: TextView
    private lateinit var tvRssnr: TextView
    private lateinit var tvCqi: TextView
    private lateinit var tvFrequency: TextView
    private lateinit var tvTimingAdvance: TextView

    private lateinit var btnBack: Button
    private lateinit var btnRefresh: Button
    private lateinit var handler: Handler

    private val UPDATE_INTERVAL = 5000L
    private var isUpdatesRunning = false
    private val updateTask = object : Runnable {
        override fun run() {
            if (isUpdatesRunning) {
                loadCellInfo()
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telephony)

        initViews()
        setupButtonListeners()

        if (hasPermissions()) {
            startUpdates()
        } else {
            requestPermissions()
        }
    }

    private fun initViews() {
        handler = Handler(Looper.getMainLooper())

        // Кнопки
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnRefresh)

        // Текстовые поля сети
        tvNetworkType = findViewById(R.id.tvNetworkType)
        tvOperator = findViewById(R.id.tvOperator)
        tvTacLac = findViewById(R.id.tvTacLac)
        tvPci = findViewById(R.id.tvPci)
        tvCid = findViewById(R.id.tvCid)
        tvSignal = findViewById(R.id.tvSignal)
        tvAsu = findViewById(R.id.tvAsu)
        tvRsrq = findViewById(R.id.tvRsrq)
        tvRssnr = findViewById(R.id.tvRssnr)
        tvCqi = findViewById(R.id.tvCqi)
        tvFrequency = findViewById(R.id.tvFrequency)
        tvTimingAdvance = findViewById(R.id.tvTimingAdvance)

        resetNetworkViews()
    }

    private fun resetNetworkViews() {
        tvNetworkType.text = "Тип: -"
        tvOperator.text = "Оператор: -"
        tvTacLac.text = "TAC/LAC: -"
        tvPci.text = "PCI: -"
        tvCid.text = "CID: -"
        tvSignal.text = "Сигнал: -"
        tvAsu.text = "ASU: -"
        tvRsrq.text = "RSRQ: -"
        tvRssnr.text = "RSSNR: -"
        tvCqi.text = "CQI: -"
        tvFrequency.text = "Частота: -"
        tvTimingAdvance.text = "Timing Advance: -"
    }

    private fun setupButtonListeners() {
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadCellInfo() }
    }

    private fun startUpdates() {
        isUpdatesRunning = true
        loadCellInfo()
        handler.postDelayed(updateTask, UPDATE_INTERVAL)
    }

    private fun stopUpdates() {
        isUpdatesRunning = false
        handler.removeCallbacks(updateTask)
    }

    private fun loadCellInfo() {
        if (!hasPermissions()) return

        try {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cells = tm.allCellInfo ?: return

            val connectedCell = cells.find { it.isRegistered }

            if (connectedCell != null) {
                updateNetworkUI(connectedCell)
            } else {
                resetNetworkViews()
                tvNetworkType.text = "Тип: Не подключено"
            }
        } catch (e: Exception) {
            resetNetworkViews()
            tvNetworkType.text = "Тип: Ошибка загрузки"
        }
    }

    private fun updateNetworkUI(cell: CellInfo) {
        when (cell) {
            is CellInfoGsm -> updateGsmUI(cell)
            is CellInfoLte -> updateLteUI(cell)
            is CellInfoWcdma -> updateWcdmaUI(cell)
            else -> resetNetworkViews()
        }
    }

    private fun updateGsmUI(info: CellInfoGsm) {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength

        tvNetworkType.text = "Тип: 2G GSM"
        tvOperator.text = "Оператор: ${safe(id.mcc)}/${safe(id.mnc)}"
        tvTacLac.text = "LAC: ${safe(id.lac)}"
        tvPci.text = "BSIC: ${safe(id.bsic)}"
        tvCid.text = "CID: ${safe(id.cid)}"
        tvSignal.text = "Сигнал: ${safeDbm(ss.dbm)}"
        tvAsu.text = "ASU: ${safe(ss.asuLevel)}"
        tvRsrq.text = "RSRQ: -"
        tvRssnr.text = "RSSNR: -"
        tvCqi.text = "CQI: -"
        tvFrequency.text = "ARFCN: ${safe(id.arfcn)}"
        tvTimingAdvance.text = "Timing Advance: ${safeTimingAdvance(ss.timingAdvance)}"
    }

    private fun updateLteUI(info: CellInfoLte) {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength

        tvNetworkType.text = "Тип: 4G LTE"
        tvOperator.text = "Оператор: ${safe(id.mcc)}/${safe(id.mnc)}"
        tvTacLac.text = "TAC: ${safe(id.tac)}"
        tvPci.text = "PCI: ${safe(id.pci)}"
        tvCid.text = "CID: ${safe(id.ci)}"
        tvSignal.text = "RSRP: ${safeDbm(ss.rsrp)}"
        tvAsu.text = "ASU: ${safe(ss.asuLevel)}"
        tvRsrq.text = "RSRQ: ${safeDb(ss.rsrq)}"
        tvRssnr.text = "RSSNR: ${safeDb(ss.rssnr)}"
        tvCqi.text = "CQI: ${safe(ss.cqi)}"
        tvFrequency.text = "EARFCN: ${safe(id.earfcn)}"
        tvTimingAdvance.text = "Timing Advance: ${safeTimingAdvance(ss.timingAdvance)}"
    }

    private fun updateWcdmaUI(info: CellInfoWcdma) {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength

        tvNetworkType.text = "Тип: 3G WCDMA"
        tvOperator.text = "Оператор: ${safe(id.mcc)}/${safe(id.mnc)}"
        tvTacLac.text = "LAC: ${safe(id.lac)}"
        tvPci.text = "PSC: ${safe(id.psc)}"
        tvCid.text = "CID: ${safe(id.cid)}"
        tvSignal.text = "Сигнал: ${safeDbm(ss.dbm)}"
        tvAsu.text = "ASU: ${safe(ss.asuLevel)}"
        tvRsrq.text = "RSRQ: -"
        tvRssnr.text = "RSSNR: -"
        tvCqi.text = "CQI: -"
        tvFrequency.text = "UARFCN: ${safe(id.uarfcn)}"
        tvTimingAdvance.text = "Timing Advance: -"
    }

    // Вспомогательные функции
    private fun safe(value: Int): String =
        if (value == Int.MAX_VALUE || value == CellInfo.UNAVAILABLE) "-" else value.toString()

    private fun safeDbm(value: Int): String =
        if (value == Int.MAX_VALUE || value == CellInfo.UNAVAILABLE) "-" else "$value dBm"

    private fun safeDb(value: Int): String =
        if (value == Int.MAX_VALUE || value == CellInfo.UNAVAILABLE) "-" else "$value dB"

    private fun safeTimingAdvance(value: Int): String =
        if (value == Int.MAX_VALUE || value == CellInfo.UNAVAILABLE) "-" else value.toString()

    private fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && hasPermissions()) {
            startUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdates()
    }
}