package com.example.progect

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class Player : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var currentTrackIndex = 0

    // Список треков и их названий (можно брать из ресурсов или метаданных)
    private val tracks = listOf(R.raw.test1, R.raw.test2)
    private val trackNames = listOf("Можно я с тобой ", "Король и Шут") // Замените на реальные названия

    // UI элементы
    private lateinit var btnPlayStop: Button
    private lateinit var seekTime: SeekBar
    private lateinit var seekVolume: SeekBar
    private lateinit var timerTextView: TextView
    private lateinit var songNameTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player)

        initViews()
        initMediaPlayer()
        setupListeners()
    }
    // Инициализация UI элементов


    private fun initViews() {
        btnPlayStop = findViewById(R.id.playstop)
        seekTime = findViewById(R.id.time)
        seekVolume = findViewById(R.id.volume)
        timerTextView = findViewById(R.id.timer)
        songNameTextView = findViewById(R.id.Name)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, tracks[currentTrackIndex])
        seekTime.max = mediaPlayer.duration
        updateTrackInfo()
    }

    private fun setupListeners() {
        // Кнопка Play/Stop
        btnPlayStop.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        // Громкость
        seekVolume.max = 100
        seekVolume.progress = 50
        mediaPlayer.setVolume(0.5f, 0.5f)
        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                mediaPlayer.setVolume(volume, volume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Перемотка
        seekTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
                updateTimerText(progress, mediaPlayer.duration)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Кнопки переключения треков
        findViewById<Button>(R.id.prev).setOnClickListener { switchTrack(-1) }
        findViewById<Button>(R.id.next).setOnClickListener { switchTrack(1) }
    }

    private fun playMusic() {
        mediaPlayer.start()
        btnPlayStop.text = "Stop"
        updateSeekBar()
    }

    private fun pauseMusic() {
        mediaPlayer.pause()
        btnPlayStop.text = "Play"
    }

    private fun switchTrack(direction: Int) {
        mediaPlayer.stop()
        mediaPlayer.release()

        currentTrackIndex = (currentTrackIndex + direction).coerceIn(0, tracks.lastIndex)
        initMediaPlayer()

        if (btnPlayStop.text == "Stop") {
            playMusic()
        }
    }

    private fun updateSeekBar() {
        runnable = Runnable {
            seekTime.progress = mediaPlayer.currentPosition
            updateTimerText(mediaPlayer.currentPosition, mediaPlayer.duration)
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
    }

    private fun updateTrackInfo() {
        songNameTextView.text = trackNames[currentTrackIndex]
        seekTime.max = mediaPlayer.duration
        updateTimerText(0, mediaPlayer.duration)
    }

    private fun updateTimerText(currentPos: Int, totalDuration: Int) {
        timerTextView.text = "${formatTime(currentPos.toLong())} / ${formatTime(totalDuration.toLong())}"
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}