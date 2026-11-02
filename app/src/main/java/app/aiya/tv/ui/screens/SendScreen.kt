package app.aiya.tv.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aiya.tv.AppViewModel
import app.aiya.tv.ui.LcarsButton
import app.aiya.tv.ui.LcarsPanel
import app.aiya.tv.ui.Step
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun SendScreen(vm: AppViewModel, onGallery: () -> Unit) {
    val url by vm.sendUrl.collectAsStateWithLifecycle()
    val media by vm.media.collectAsStateWithLifecycle()

    Row(Modifier.fillMaxSize().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val u = url
            if (u == null) {
                Box(Modifier.size(260.dp).clip(RoundedCornerShape(16.dp)).background(P.panel), contentAlignment = Alignment.Center) {
                    LText("NO NETWORK", size = 26.sp, color = P.red, bold = true)
                }
            } else {
                val qr = remember(u) { qrBitmap(u, 600) }
                Box(Modifier.size(260.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(14.dp)) {
                    Image(qr.asImageBitmap(), contentDescription = "QR code for $u", Modifier.fillMaxSize(), filterQuality = FilterQuality.None)
                }
            }
            LText(u?.removePrefix("http://")?.trimEnd('/') ?: "CONNECT TO WI-FI", size = 26.sp, bold = true, color = P.orange)
            LcarsButton("REFRESH", P.blue, vm::refreshAll, Modifier.size(width = 200.dp, height = 44.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LcarsPanel("SEND FROM IPHONE", P.lilac) {
                Step(1, "OPEN THE IPHONE CAMERA AND POINT IT AT THE QR CODE.", P.orange)
                Step(2, "TAP THE LINK THAT APPEARS. IT OPENS IN SAFARI.", P.lilac)
                Step(3, "TAP PHOTOS & VIDEOS AND PICK WHAT TO SHOW.", P.blue)
                Step(4, "IT APPEARS ON THE TV AS SOON AS IT ARRIVES. LINKS (YOUTUBE, WEB) WORK TOO.", P.peach)
            }
            LText("THE IPHONE MUST BE ON THE SAME WI-FI. FILES STAY ON THIS TV ONLY.", size = 15.sp, color = P.dim, maxLines = 2)
            LcarsButton("GALLERY · ${media.size} ${if (media.size == 1) "ITEM" else "ITEMS"}", P.gold, onGallery, Modifier.fillMaxWidth(0.6f), height = 48.dp)
        }
    }
}

private fun qrBitmap(text: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 1))
    val pixels = IntArray(size * size) { i -> if (matrix[i % size, i / size]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}
