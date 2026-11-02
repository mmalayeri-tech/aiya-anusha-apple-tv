package app.aiya.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aiya.tv.AppViewModel
import app.aiya.tv.Incoming
import app.aiya.tv.ui.screens.GalleryScreen
import app.aiya.tv.ui.screens.HomeScreen
import app.aiya.tv.ui.screens.MirrorScreen
import app.aiya.tv.ui.screens.SendScreen
import app.aiya.tv.ui.screens.ServicesScreen
import app.aiya.tv.ui.screens.ViewerScreen
import app.aiya.tv.ui.screens.WebScreen
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Section(val title: String, val code: String) {
    HOME("MAIN SCREEN", "01-2611"),
    APPLE("APPLE SERVICES", "02-1984"),
    MIRROR("AIRPLAY MIRROR", "03-0707"),
    SEND("SEND FROM IPHONE", "04-4040"),
    GALLERY("GALLERY", "05-5150"),
}

/** Full-screen layers that sit on top of the LCARS frame. */
sealed interface Overlay {
    data class Web(val url: String) : Overlay
    data class Viewer(val start: Int) : Overlay
}

const val APP_TITLE = "AIYA ANUSHA APPLE TV"

private val SidebarWidth = 170.dp
private val ElbowExtra = 34.dp
private val ArmHeight = 18.dp
private val TopHeight = 70.dp
private val BottomHeight = 52.dp

@Composable
fun AiyaApp(vm: AppViewModel, onExit: () -> Unit) {
    val toast by vm.toast.collectAsStateWithLifecycle()
    val media by vm.media.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf(Section.HOME) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var focusContent by remember { mutableStateOf(false) }
    val navRequesters = remember { Section.entries.associateWith { FocusRequester() } }
    val contentRequester = remember { FocusRequester() }

    fun navigate(s: Section) {
        section = s
        focusContent = true
    }

    LaunchedEffect(Unit) {
        delay(150)
        runCatching { navRequesters.getValue(section).requestFocus() }
    }
    LaunchedEffect(section, focusContent) {
        if (focusContent) {
            delay(320)
            runCatching { contentRequester.requestFocus() }
            focusContent = false
        }
    }
    // Something arrived from the iPhone: show it straight away, like casting.
    LaunchedEffect(Unit) {
        vm.incoming.collect { event ->
            when (event) {
                is Incoming.Media -> {
                    vm.say("INCOMING: ${event.item.name.uppercase()}")
                    val list = vm.media.value
                    overlay = Overlay.Viewer(list.indexOfFirst { it.file == event.item.file }.coerceAtLeast(0))
                }
                is Incoming.Link -> {
                    vm.say("INCOMING LINK")
                    if (!vm.launcher.openExternally(event.url)) overlay = Overlay.Web(event.url)
                }
            }
        }
    }

    BackHandler(enabled = overlay == null) {
        if (section != Section.HOME) {
            section = Section.HOME
            runCatching { navRequesters.getValue(Section.HOME).requestFocus() }
        } else {
            onExit()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        LcarsFrame(
            title = section.title,
            code = section.code,
            sidebar = {
                Section.entries.forEachIndexed { i, s ->
                    LcarsButton(
                        text = s.title,
                        color = P.pick(i + 1),
                        onClick = { navigate(s) },
                        modifier = Modifier.fillMaxWidth().focusRequester(navRequesters.getValue(s)),
                        shape = RectangleShape,
                        height = 42.dp,
                        textSize = 15,
                        selected = s == section,
                    )
                }
            },
            navEnter = { navRequesters.getValue(section) },
        ) {
            AnimatedContent(
                targetState = section,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInHorizontally(tween(300)) { it / 12 }) togetherWith fadeOut(tween(160))
                },
                label = "section",
                modifier = Modifier.fillMaxSize().focusRequester(contentRequester).focusGroup(),
            ) { target ->
                when (target) {
                    Section.HOME -> HomeScreen(vm, onNavigate = ::navigate, onWeb = { overlay = Overlay.Web(it) })
                    Section.APPLE -> ServicesScreen(vm, onWeb = { overlay = Overlay.Web(it) })
                    Section.MIRROR -> MirrorScreen(vm)
                    Section.SEND -> SendScreen(vm, onGallery = { navigate(Section.GALLERY) })
                    Section.GALLERY -> GalleryScreen(vm, onOpen = { overlay = Overlay.Viewer(it) })
                }
            }
        }

        when (val o = overlay) {
            is Overlay.Web -> WebScreen(o.url, onClose = { overlay = null })
            is Overlay.Viewer -> if (media.isNotEmpty()) {
                ViewerScreen(media, o.start.coerceIn(0, media.lastIndex), onClose = { overlay = null }, onDelete = vm::delete)
            } else {
                LaunchedEffect(Unit) { overlay = null }
            }
            null -> Unit
        }

        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 84.dp),
        ) {
            LText(
                toast.orEmpty(),
                Modifier.clip(RoundedCornerShape(20.dp)).background(P.peach).padding(horizontal = 24.dp, vertical = 8.dp),
                size = 20.sp, color = Color.Black, bold = true,
            )
        }
    }
}

@Composable
private fun rememberNow(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    return now
}

@Composable
private fun LcarsFrame(
    title: String,
    code: String,
    sidebar: @Composable () -> Unit,
    navEnter: () -> FocusRequester,
    content: @Composable () -> Unit,
) {
    val now = rememberNow()
    val clock = remember { SimpleDateFormat("EEE dd MMM · HH:mm:ss", Locale.getDefault()) }
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(Unit) { sweep.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val sweepLayer = Modifier.graphicsLayer {
        scaleX = sweep.value
        transformOrigin = TransformOrigin(0f, 0.5f)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth().height(TopHeight)) {
            Box(
                Modifier.width(SidebarWidth + ElbowExtra).height(TopHeight)
                    .background(P.orange, ElbowShape(SidebarWidth, ArmHeight, 40.dp, 18.dp)),
            ) {
                LText(
                    "LCARS $code",
                    Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 4.dp).width(SidebarWidth - 20.dp),
                    size = 14.sp, color = Color.Black, bold = true, align = TextAlign.End,
                )
            }
            Row(Modifier.weight(1f).height(TopHeight), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(sweepLayer.fillMaxWidth().height(ArmHeight)) {
                        Bar(P.orange, Modifier.weight(3f).height(ArmHeight))
                        Spacer(Modifier.width(6.dp))
                        Bar(P.lilac, Modifier.weight(1f).height(ArmHeight))
                        Spacer(Modifier.width(6.dp))
                        Bar(P.blue, Modifier.width(48.dp).height(ArmHeight))
                        Spacer(Modifier.width(6.dp))
                    }
                    LText(APP_TITLE, Modifier.padding(start = 16.dp, top = 8.dp), size = 20.sp, color = P.lilac, bold = true)
                }
                LText(title, Modifier.padding(horizontal = 10.dp), size = 40.sp, color = P.orange, bold = true)
                Bar(P.peach, Modifier.width(30.dp).height(ArmHeight), RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp))
            }
        }

        Row(Modifier.fillMaxWidth().weight(1f).padding(vertical = 5.dp)) {
            Column(
                Modifier.width(SidebarWidth).focusProperties { enter = { navEnter() } }.focusGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                sidebar()
                Bar(P.blue, Modifier.fillMaxWidth().weight(1f))
            }
            Spacer(Modifier.width(ElbowExtra))
            Box(Modifier.weight(1f).padding(horizontal = 4.dp)) { content() }
        }

        Row(Modifier.fillMaxWidth().height(BottomHeight)) {
            Box(
                Modifier.width(SidebarWidth + ElbowExtra).height(BottomHeight)
                    .background(P.lilac, ElbowShape(SidebarWidth, ArmHeight, 34.dp, 16.dp, flip = true)),
            )
            Row(Modifier.weight(1f).height(BottomHeight), verticalAlignment = Alignment.Bottom) {
                Bar(P.lilac, sweepLayer.width(70.dp).height(ArmHeight))
                Spacer(Modifier.width(10.dp))
                LText("FOR AIYA & ANUSHA", size = 15.sp, color = P.peach, bold = true)
                Spacer(Modifier.width(10.dp))
                Bar(P.orange, sweepLayer.weight(1f).height(ArmHeight))
                Spacer(Modifier.width(8.dp))
                LText(clock.format(Date(now)).uppercase(), size = 15.sp)
                Spacer(Modifier.width(8.dp))
                Bar(P.blue, Modifier.width(30.dp).height(ArmHeight), RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp))
            }
        }
    }
}
