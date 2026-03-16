package com.wlaxid.scriptflow.ui.overlay

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wlaxid.scriptflow.R

class ScreenCaptureService : Service() {

    companion object {
        var mediaProjection: MediaProjection? = null
        var onProjectionReady: ((MediaProjection) -> Unit)? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) return START_NOT_STICKY

        val resultCode = intent.getIntExtra("code", Activity.RESULT_CANCELED)
        val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("data")
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceProperly()

        val manager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjection = manager.getMediaProjection(resultCode, data)

        mediaProjection?.let {
            onProjectionReady?.invoke(it)
        }

        return START_STICKY
    }

    private fun startForegroundServiceProperly() {

        val channelId = "capture_channel"

        val channel = NotificationChannel(
            channelId,
            "Screen Capture",
            NotificationManager.IMPORTANCE_LOW
        )

        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen capture running")
            .setSmallIcon(R.drawable.ic_screenshot)
            .build()

        startForeground(
            1,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}