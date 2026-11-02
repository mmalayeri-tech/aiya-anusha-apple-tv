package app.aiya.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aiya.tv.AppViewModel
import app.aiya.tv.data.Catalog
import app.aiya.tv.ui.LcarsButton
import app.aiya.tv.ui.LcarsPanel
import app.aiya.tv.ui.LcarsTile
import app.aiya.tv.ui.Step
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P

@Composable
fun MirrorScreen(vm: AppViewModel) {
    val receivers by vm.receivers.collectAsStateWithLifecycle()

    Row(Modifier.fillMaxSize().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (receivers.isEmpty()) {
                LText("NO AIRPLAY RECEIVER INSTALLED", size = 28.sp, bold = true, color = P.red)
                LText(
                    "ANDROID TV CAN'T RECEIVE AIRPLAY ON ITS OWN. INSTALL A FREE RECEIVER ONCE, THEN START IT FROM HERE.",
                    size = 18.sp, maxLines = 3,
                )
                val r = Catalog.receivers.first()
                LcarsTile("INSTALL ${r.title}", r.note, "PLAY STORE", P.orange, onClick = vm::installReceiver, modifier = Modifier.fillMaxWidth())
                LcarsButton("SCAN AGAIN", P.blue, vm::refreshAll, Modifier.fillMaxWidth(0.5f), height = 48.dp)
            } else {
                LText("START MIRRORING", size = 28.sp, bold = true, color = P.orange)
                receivers.forEachIndexed { i, app ->
                    LcarsTile(app.label, "PRESS OK TO START THE RECEIVER", "START", P.pick(i), onClick = { vm.openReceiver(app) }, modifier = Modifier.fillMaxWidth())
                }
                LcarsButton("SCAN AGAIN", P.blue, vm::refreshAll, Modifier.fillMaxWidth(0.5f), height = 48.dp)
            }
        }
        Column(Modifier.weight(1f)) {
            LcarsPanel("ON YOUR IPHONE", P.lilac) {
                Step(1, "CONNECT THE IPHONE TO THE SAME WI-FI AS THE TV.", P.orange)
                Step(2, "START THE RECEIVER ON THE LEFT AND LEAVE IT OPEN.", P.lilac)
                Step(3, "SWIPE DOWN FROM THE TOP-RIGHT CORNER TO OPEN CONTROL CENTER.", P.blue)
                Step(4, "TAP SCREEN MIRRORING, THEN PICK THE TV BY NAME.", P.peach)
                Step(5, "TYPE THE CODE SHOWN ON THE TV IF ASKED. TO STOP, TAP STOP MIRRORING.", P.gold)
            }
            LText(
                "TIP: IN PHOTOS OR VIDEOS, THE AIRPLAY BUTTON SENDS JUST THAT VIDEO, NOT YOUR WHOLE SCREEN.",
                Modifier.padding(top = 14.dp), size = 15.sp, color = P.dim, maxLines = 3,
            )
        }
    }
}
