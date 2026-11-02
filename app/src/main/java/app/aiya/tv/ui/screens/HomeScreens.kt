package app.aiya.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aiya.tv.AppViewModel
import app.aiya.tv.data.Catalog
import app.aiya.tv.ui.InfoLine
import app.aiya.tv.ui.LcarsButton
import app.aiya.tv.ui.LcarsPanel
import app.aiya.tv.ui.LcarsTile
import app.aiya.tv.ui.Section
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P

@Composable
fun HomeScreen(vm: AppViewModel, onNavigate: (Section) -> Unit, onWeb: (String) -> Unit) {
    val installed by vm.installed.collectAsStateWithLifecycle()
    val receivers by vm.receivers.collectAsStateWithLifecycle()
    val media by vm.media.collectAsStateWithLifecycle()
    val sendUrl by vm.sendUrl.collectAsStateWithLifecycle()
    val tv = Catalog.services.first { it.id == "tv" }
    val music = Catalog.services.first { it.id == "music" }

    Row(Modifier.fillMaxSize().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LText("WELCOME, AIYA & ANUSHA", size = 34.sp, bold = true, color = P.orange)
            LcarsTile(
                "APPLE TV+", "WATCH SHOWS AND MOVIES", if ("tv" in installed) "OPEN" else "WEB",
                P.orange, onClick = { vm.openService(tv)?.let(onWeb) }, modifier = Modifier.fillMaxWidth(),
            )
            LcarsTile(
                "MIRROR IPHONE", "AIRPLAY YOUR SCREEN TO THE TV", if (receivers.isNotEmpty()) "READY" else "SET UP",
                P.blue, onClick = { onNavigate(Section.MIRROR) }, modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LcarsTile(
                    "MUSIC", "APPLE MUSIC", if ("music" in installed) "OPEN" else "WEB",
                    P.lilac, onClick = { vm.openService(music)?.let(onWeb) }, modifier = Modifier.weight(1f), height = 96.dp,
                )
                LcarsTile(
                    "SEND", "PHOTOS · VIDEOS", "QR", P.peach,
                    onClick = { onNavigate(Section.SEND) }, modifier = Modifier.weight(1f), height = 96.dp,
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            LcarsPanel("SYSTEM STATUS", P.lilac) {
                InfoLine("APPLE TV APP", if ("tv" in installed) "INSTALLED" else "NOT INSTALLED", if ("tv" in installed) P.peach else P.red)
                InfoLine("APPLE MUSIC APP", if ("music" in installed) "INSTALLED" else "NOT INSTALLED", if ("music" in installed) P.peach else P.red)
                InfoLine("AIRPLAY RECEIVER", receivers.firstOrNull()?.label ?: "NONE", if (receivers.isNotEmpty()) P.peach else P.red)
                InfoLine("SEND ADDRESS", sendUrl?.removePrefix("http://")?.trimEnd('/') ?: "OFFLINE")
                InfoLine("GALLERY", "${media.size} ${if (media.size == 1) "ITEM" else "ITEMS"}")
            }
            LcarsButton("ALL APPLE SERVICES", P.gold, { onNavigate(Section.APPLE) }, Modifier.fillMaxWidth(), height = 50.dp, textSize = 20)
            LcarsButton("OPEN GALLERY", P.blue, { onNavigate(Section.GALLERY) }, Modifier.fillMaxWidth(), height = 50.dp, textSize = 20)
        }
    }
}

@Composable
fun ServicesScreen(vm: AppViewModel, onWeb: (String) -> Unit) {
    val installed by vm.installed.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(Catalog.services) { i, s ->
            val has = s.id in installed
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LcarsTile(
                    s.title, s.subtitle,
                    when {
                        has -> "OPEN APP"
                        s.webUrl != null -> "OPEN WEB"
                        else -> "GET APP"
                    },
                    P.pick(i),
                    onClick = { vm.openService(s)?.let(onWeb) ?: run { if (!has) vm.getApp(s) } },
                    modifier = Modifier.weight(1f), height = 92.dp,
                )
                if (!has && s.storePackage != null) {
                    LcarsButton("GET APP", P.pick(i + 2), { vm.getApp(s) }, Modifier.width(150.dp).padding(top = 22.dp), height = 48.dp)
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            LText(
                "APPS OPEN DIRECTLY WHEN INSTALLED. OTHERWISE THEY OPEN IN THE BUILT-IN WEB VIEW. BACK CLOSES IT.",
                size = 15.sp, color = P.dim, maxLines = 2,
            )
        }
    }
}
