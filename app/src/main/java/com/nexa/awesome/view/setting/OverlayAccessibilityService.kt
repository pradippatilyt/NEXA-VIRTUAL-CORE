package com.nexa.awesome.view.setting

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.net.Uri
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.VideoView
import com.nexa.awesome.camera.MultiPreferences

class OverlayAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var videoView: VideoView? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        if (isCameraApp(packageName)) {
            showVideoOverlay()
        } else {
            removeVideoOverlay()
        }
    }

    private fun isCameraApp(packageName: String): Boolean {
        return packageName.contains("whatsapp") || packageName.contains("camera") || packageName.contains("instagram")
    }

    private fun showVideoOverlay() {
        if (videoView != null) return

        val prefs = MultiPreferences.getInstance()
        val methodType = prefs.getInt("method_type", 0)

        val videoUri = when (methodType) {
            1 -> Uri.parse(prefs.getString("video_path_local", ""))
            2 -> Uri.parse(prefs.getString("video_path_network", ""))
            else -> null
        } ?: return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        videoView = VideoView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager?.addView(videoView, params)
        videoView?.apply {
            setVideoURI(videoUri)
            setOnPreparedListener { mp ->
                mp.isLooping = true
                start()
            }
        }
    }

    private fun removeVideoOverlay() {
        if (videoView != null) {
            windowManager?.removeView(videoView)
            videoView = null
        }
    }

    override fun onInterrupt() {
        removeVideoOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeVideoOverlay()
    }
}
