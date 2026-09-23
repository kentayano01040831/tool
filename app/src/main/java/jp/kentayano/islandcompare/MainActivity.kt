package jp.kentayano.islandcompare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CompareScreen()
                }
            }
        }
    }
}

@Composable
private fun CompareScreen() {
    var priceA by remember { mutableStateOf("") }
    var quantityA by remember { mutableStateOf("") }
    var priceB by remember { mutableStateOf("") }
    var quantityB by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("価格と数量を入力してください") }

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Island Compare", style = MaterialTheme.typography.headlineMedium)
        Text("商品の単価を比較")

        OutlinedTextField(priceA, { priceA = it }, label = { Text("商品Aの価格（円）") })
        OutlinedTextField(quantityA, { quantityA = it }, label = { Text("商品Aの数量") })
        OutlinedTextField(priceB, { priceB = it }, label = { Text("商品Bの価格（円）") })
        OutlinedTextField(quantityB, { quantityB = it }, label = { Text("商品Bの数量") })

        Button(onClick = {
            val a = priceA.toDoubleOrNull()?.div(quantityA.toDoubleOrNull() ?: 0.0)
            val b = priceB.toDoubleOrNull()?.div(quantityB.toDoubleOrNull() ?: 0.0)
            result = if (a == null || b == null || !a.isFinite() || !b.isFinite()) {
                "正しい価格と数量を入力してください"
            } else {
                val cheaper = if (a < b) "商品A" else if (b < a) "商品B" else "同額"
                "商品A：%.2f円/個\n商品B：%.2f円/個\n安い商品：%s".format(a, b, cheaper)
            }
        }) {
            Text("比較する")
        }

        Text(result, style = MaterialTheme.typography.bodyLarge)
    }
}
