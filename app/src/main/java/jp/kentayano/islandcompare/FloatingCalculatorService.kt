package jp.kentayano01040831.islandcompare

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

/** Interactive calculator panel shown without opening the main Activity. */
class FloatingCalculatorService : Service() {
    private var wm: WindowManager? = null
    private var panel: LinearLayout? = null
    private val channelId = "floating_calculator"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("単価比較")
            .setContentText("フローティング計算機を表示中")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(1001, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        if (panel == null) showPanel()
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "単価比較", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showPanel() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.rgb(28, 28, 32))
        }
        val title = TextView(this).apply {
            text = "単価比較  ⋮  長押しで移動"
            textSize = 18f; setTextColor(Color.WHITE); setPadding(0, 0, 0, 8)
        }
        root.addView(title)
        val aPrice = field("商品A 価格")
        val aQty = field("商品A 数量")
        val bPrice = field("商品B 価格")
        val bQty = field("商品B 数量")
        listOf(aPrice, aQty, bPrice, bQty).forEach { root.addView(it) }
        val result = TextView(this).apply { setTextColor(Color.WHITE); textSize = 15f; setPadding(0, 8, 0, 8) }
        root.addView(result)
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val calc = Button(this).apply {
            text = "計算"
            setOnClickListener {
                val ap = aPrice.text.toString().toDoubleOrNull(); val aq = aQty.text.toString().toDoubleOrNull()
                val bp = bPrice.text.toString().toDoubleOrNull(); val bq = bQty.text.toString().toDoubleOrNull()
                if (ap == null || aq == null || bp == null || bq == null || aq <= 0 || bq <= 0 || ap < 0 || bp < 0) {
                    result.text = "価格と数量を正しく入力してください"
                } else {
                    val ua = ap / aq; val ub = bp / bq
                    val cheaper = if (ua < ub) "商品A" else if (ub < ua) "商品B" else "同額"
                    result.text = "A: %.2f円/個\nB: %.2f円/個\n安い方: %s\n差額: %.2f円/個".format(ua, ub, cheaper, abs(ua - ub))
                }
            }
        }
        val close = Button(this).apply { text = "閉じる"; setOnClickListener { stopSelf() } }
        buttons.addView(calc, LinearLayout.LayoutParams(0, -2, 1f)); buttons.addView(close, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(buttons)
        panel = root
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM.inv(),
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP; y = 48; softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE }
        wm?.addView(panel, params)
    }

    private fun field(hint: String) = EditText(this).apply {
        this.hint = hint; setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); textSize = 16f
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        setSingleLine(true)
    }

    override fun onDestroy() { panel?.let { wm?.removeView(it) }; panel = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
