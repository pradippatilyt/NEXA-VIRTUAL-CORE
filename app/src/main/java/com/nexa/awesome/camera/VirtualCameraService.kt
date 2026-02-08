package com.nexa.awesome.camera

import android.app.Service
import android.net.Uri
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.view.Surface
import java.util.concurrent.Executors

class VirtualCameraService : Service() {
    private lateinit var cameraManager: CameraManager
    private var mediaPlayer: MediaPlayer? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val videoPath = it.getStringExtra("video_path") ?: return START_NOT_STICKY
            val isNetwork = it.getBooleanExtra("is_network", false)
            val enableAudio = it.getBooleanExtra("enable_audio", false)
            
            if (isRunning) {
                stopMediaPlayer()
            }
            
            executor.execute {
                try {
                    setupVirtualCamera(videoPath, isNetwork, enableAudio)
                    isRunning = true
                } catch (e: Exception) {
                    Log.e("VirtualCamera", "Error setting up virtual camera", e)
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun setupVirtualCamera(videoPath: String, isNetwork: Boolean, enableAudio: Boolean) {
        mediaPlayer = if (isNetwork) {
            MediaPlayer().apply {
                setDataSource(videoPath)
                setOnPreparedListener { start() }
                prepareAsync()
            }
        } else {
            MediaPlayer.create(this, Uri.parse(videoPath))
        }

        mediaPlayer?.apply {
            isLooping = true
            setVolume(if (enableAudio) 1.0f else 0.0f, if (enableAudio) 1.0f else 0.0f)
            
            // Create a surface for the virtual camera
            val surfaceTexture = SurfaceTexture(0)
            val surface = Surface(surfaceTexture)
            setSurface(surface)
            
            start()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
        isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMediaPlayer()
        executor.shutdown()
    }
}