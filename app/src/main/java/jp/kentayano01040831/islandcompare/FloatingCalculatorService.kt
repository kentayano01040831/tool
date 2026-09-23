package jp.kentayano.islandcompare

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*

class FloatingCalculatorService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var calculatorView: View

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, buildNotification())
        showCalculator()
    }

    private fun showCalculator() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val input = EditText(this).apply {
            hint = "計算式を入力"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        val result = TextView(this).apply { textSize = 20f }
        val button = Button(this).apply { text = "計算" }
        button.setOnClickListener {
            result.text = try {
                val expression = input.text.toString()
                "= ${evaluate(expression)}"
            } catch (e: Exception) {
                "計算できません"
            }
        }
        layout.addView(input)
        layout.addView(button)
        layout.addView(result)
        calculatorView = layout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(calculatorView, params)
    }

    private fun evaluate(expression: String): Double {
        return expression.toDouble()
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, "calculator_channel")
            .setContentTitle("Floating Calculator")
            .setContentText("電卓を表示中")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "calculator_channel",
                "Floating Calculator",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::calculatorView.isInitialized) windowManager.removeView(calculatorView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}