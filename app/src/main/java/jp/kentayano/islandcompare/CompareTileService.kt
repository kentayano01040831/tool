package jp.kentayano.islandcompare

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

/** Quick Settings tile: opens the calculator directly as a floating panel. */
class CompareTileService : TileService() {
    override fun onClick() {
        super.onClick()
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapse(intent)
            return
        }
        val serviceIntent = Intent(this, FloatingCalculatorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.label = "単価比較"
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }
}
