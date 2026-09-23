package jp.kentayano.islandcompare

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

/** A calculator panel shown without opening the main Activity. */
class FloatingCalculatorService : Service() {
    private var wm: WindowManager? = null
    private var panel: LinearLayout? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (panel == null) showPanel()
        return START_NOT_STICKY
    }

    private fun showPanel() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
            setBackgroundColor(Color.rgb(25, 25, 28))
        }
        val title = TextView(this).apply {
            text = "単価比較"
            textSize = 19f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 12)
        }
        root.addView(title)
        val aPrice = field("商品A 価格")
        val aQty = field("商品A 数量")
        val bPrice = field("商品B 価格")
        val bQty = field("商品B 数量")
        root.addView(aPrice); root.addView(aQty); root.addView(bPrice); root.addView(bQty)
        val result = TextView(this).apply { setTextColor(Color.WHITE); textSize = 15f; setPadding(0, 12, 0, 8) }
        root.addView(result)
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val calc = Button(this).apply {
            text = "計算"
            setOnClickListener {
                val ap = aPrice.text.toString().toDoubleOrNull()
                val aq = aQty.text.toString().toDoubleOrNull()
                val bp = bPrice.text.toString().toDoubleOrNull()
                val bq = bQty.text.toString().toDoubleOrNull()
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
        buttons.addView(calc, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        buttons.addView(close, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(buttons)
        panel = root
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP; y = 48 }
        wm?.addView(panel, params)
    }

    private fun field(hint: String): EditText = EditText(this).apply {
        this.hint = hint; setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); textSize = 16f
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    override fun onDestroy() { panel?.let { wm?.removeView(it) }; panel = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
