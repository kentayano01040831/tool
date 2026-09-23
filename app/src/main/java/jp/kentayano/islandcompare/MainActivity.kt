package jp.kentayano.islandcompare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { CompareScreen() }
            }
        }
    }
}

@Composable
private fun CompareScreen() {
    val context = LocalContext.current
    var nameA by remember { mutableStateOf("商品A") }
    var priceA by remember { mutableStateOf("") }
    var quantityA by remember { mutableStateOf("") }
    var nameB by remember { mutableStateOf("商品B") }
    var priceB by remember { mutableStateOf("") }
    var quantityB by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    fun calculate(): String? {
        val pA = priceA.toDoubleOrNull()
        val qA = quantityA.toDoubleOrNull()
        val pB = priceB.toDoubleOrNull()
        val qB = quantityB.toDoubleOrNull()
        if (pA == null || qA == null || pA < 0 || qA <= 0 || pB == null || qB == null || pB < 0 || qB <= 0) return null
        val unitA = pA / qA
        val unitB = pB / qB
        val cheaper = when {
            unitA < unitB -> nameA
            unitB < unitA -> nameB
            else -> "同額"
        }
        return "$nameA：%.2f円/個\n$nameB：%.2f円/個\n安い商品：$cheaper\n差額：%.2f円/個".format(unitA, unitB, kotlin.math.abs(unitA - unitB))
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Island Compare", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("価格と数量から、1個あたりの単価を比較します。")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("商品A", fontWeight = FontWeight.Bold)
                OutlinedTextField(nameA, { nameA = it }, label = { Text("商品名") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(priceA, { priceA = it }, label = { Text("価格（円）") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(quantityA, { quantityA = it }, label = { Text("数量") }, modifier = Modifier.weight(1f))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("商品B", fontWeight = FontWeight.Bold)
                OutlinedTextField(nameB, { nameB = it }, label = { Text("商品名") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(priceB, { priceB = it }, label = { Text("価格（円）") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(quantityB, { quantityB = it }, label = { Text("数量") }, modifier = Modifier.weight(1f))
                }
            }
        }

        Button(onClick = { result = calculate() }, modifier = Modifier.fillMaxWidth()) { Text("単価を比較する") }

        result?.let { text ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = {
                        if (!Settings.canDrawOverlays(context)) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                        } else {
                            context.startService(Intent(context, FloatingResultService::class.java).putExtra(FloatingResultService.EXTRA_TEXT, text))
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("画面上部に表示") }
                }
            }
        }
    }
}
