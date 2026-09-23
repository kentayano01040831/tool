package jp.kentayano.islandcompare

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class FloatingResultService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: TextView? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Island Compare")
            .setContentText("比較結果を表示中")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1001, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra(EXTRA_TEXT) ?: return START_NOT_STICKY
        showOverlay(text)
        return START_NOT_STICKY
    }

    private fun showOverlay(text: String) {
        if (overlay == null) {
            overlay = TextView(this).apply {
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.BLACK)
                setPadding(36, 18, 36, 18)
                textSize = 14f
                gravity = Gravity.CENTER
            }
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 48
            }
            windowManager?.addView(overlay, params)
        }
        overlay?.text = text
    }

    override fun onDestroy() {
        overlay?.let { windowManager?.removeView(it) }
        overlay = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Island Compare",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val EXTRA_TEXT = "extra_text"
        private const val CHANNEL_ID = "island_compare"
    }
}
