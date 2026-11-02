package app.aiya.tv.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P

/** Built-in browser for Apple services that have no Android TV app. Back goes back, then closes. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(url: String, onClose: () -> Unit) {
    var web by remember { mutableStateOf<WebView?>(null) }
    var title by remember { mutableStateOf(url) }

    BackHandler {
        val w = web
        if (w != null && w.canGoBack()) w.goBack() else onClose()
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxWidth().height(34.dp).padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(60.dp).height(22.dp).background(P.orange, RoundedCornerShape(topStart = 11.dp, bottomStart = 11.dp)))
            Spacer(Modifier.width(8.dp))
            LText(title.uppercase(), Modifier.weight(1f), size = 16.sp, bold = true)
            LText("BACK TO CLOSE", size = 14.sp, color = P.dim)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.width(30.dp).height(22.dp).background(P.blue, RoundedCornerShape(topEnd = 11.dp, bottomEnd = 11.dp)))
        }
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    // A desktop browser identity: some Apple sites refuse unknown or TV browsers.
                    settings.userAgentString =
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0 Safari/537.36"
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onReceivedTitle(view: WebView?, t: String?) { if (!t.isNullOrBlank()) title = t }
                    }
                    loadUrl(url)
                    requestFocus()
                    web = this
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
            onRelease = { it.destroy() },
        )
    }
}
