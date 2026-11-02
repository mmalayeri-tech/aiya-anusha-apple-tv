package app.aiya.tv.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.os.Build
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import app.aiya.tv.AppViewModel
import app.aiya.tv.data.MediaItem
import app.aiya.tv.data.MediaKind
import app.aiya.tv.ui.LcarsButton
import app.aiya.tv.ui.theme.LText
import app.aiya.tv.ui.theme.P
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GalleryScreen(vm: AppViewModel, onOpen: (Int) -> Unit) {
    val media by vm.media.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LText("${media.size} ${if (media.size == 1) "ITEM" else "ITEMS"} FROM IPHONE", Modifier.weight(1f), size = 24.sp, bold = true, color = P.orange)
            if (media.isNotEmpty()) {
                LcarsButton("SLIDESHOW", P.blue, { onOpen(0) }, Modifier.width(170.dp))
                LcarsButton("CLEAR ALL", P.red, vm::clearMedia, Modifier.width(170.dp))
            }
        }
        if (media.isEmpty()) {
            LText("NOTHING HERE YET. USE SEND FROM IPHONE TO SHARE PHOTOS AND VIDEOS.", size = 20.sp, color = P.dim, maxLines = 2)
        }
        LazyVerticalGrid(GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            itemsIndexed(media, key = { _, m -> m.file.path }) { i, m ->
                Surface(
                    onClick = { onOpen(i) },
                    modifier = Modifier.height(130.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = P.panel, focusedContainerColor = P.panel),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(
                        border = Border(BorderStroke(2.dp, P.pick(i).copy(alpha = 0.6f))),
                        focusedBorder = Border(BorderStroke(4.dp, Color.White)),
                    ),
                ) {
                    val thumb by produceState<Bitmap?>(null, m.file) { value = withContext(Dispatchers.IO) { thumbnail(m, 400) } }
                    thumb?.let { Image(it.asImageBitmap(), m.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    LText(
                        if (m.kind == MediaKind.VIDEO) "▶ ${m.name}" else m.name,
                        Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 8.dp, vertical = 2.dp),
                        size = 13.sp,
                    )
                }
            }
        }
    }
}

/** Full-screen photo / video player with an LCARS control bar. */
@Composable
fun ViewerScreen(items: List<MediaItem>, start: Int, onClose: () -> Unit, onDelete: (MediaItem) -> Unit) {
    var index by remember(start) { mutableIntStateOf(start) }
    var slideshow by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(true) }
    val item = items[index.coerceIn(0, items.lastIndex)]
    val firstButton = remember { FocusRequester() }
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    fun step(d: Int) { index = (index + d).mod(items.size); playing = true }

    BackHandler(onBack = onClose)
    LaunchedEffect(Unit) { runCatching { firstButton.requestFocus() } }
    LaunchedEffect(slideshow, index) {
        if (slideshow && item.kind == MediaKind.IMAGE) {
            delay(6000)
            step(1)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        key(item.file) {
            when (item.kind) {
                MediaKind.IMAGE -> {
                    val bmp by produceState<Bitmap?>(null, item.file) { value = withContext(Dispatchers.IO) { decodeImage(item.file, 1920) } }
                    bmp?.let { Image(it.asImageBitmap(), item.name, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                        ?: LText("LOADING…", Modifier.align(Alignment.Center), size = 28.sp, color = P.orange)
                }
                MediaKind.VIDEO, MediaKind.AUDIO -> AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoPath(item.file.path)
                            setOnPreparedListener { it.isLooping = false; start() }
                            setOnCompletionListener { playing = false; if (slideshow) step(1) }
                            setOnErrorListener { _, _, _ -> playing = false; true }
                            videoView = this
                        }
                    },
                    modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                    onRelease = { it.stopPlayback(); if (videoView === it) videoView = null },
                )
                MediaKind.OTHER -> LText("CAN'T SHOW ${item.name.uppercase()}", Modifier.align(Alignment.Center), size = 28.sp, color = P.red)
            }
        }

        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.width(40.dp).height(36.dp).background(P.orange, RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)))
            LText("${index + 1}/${items.size} · ${item.name.uppercase()}", Modifier.weight(1f), size = 18.sp, bold = true)
            LcarsButton("◀ PREV", P.lilac, { step(-1) }, Modifier.width(110.dp).focusRequester(firstButton), height = 40.dp)
            if (item.kind == MediaKind.VIDEO || item.kind == MediaKind.AUDIO) {
                LcarsButton(if (playing) "PAUSE" else "PLAY", P.orange, {
                    videoView?.let { if (it.isPlaying) it.pause() else it.start(); playing = it.isPlaying }
                }, Modifier.width(110.dp), height = 40.dp)
            }
            LcarsButton("NEXT ▶", P.lilac, { step(1) }, Modifier.width(110.dp), height = 40.dp)
            LcarsButton(if (slideshow) "STOP SHOW" else "SLIDESHOW", P.blue, { slideshow = !slideshow }, Modifier.width(140.dp), height = 40.dp)
            LcarsButton("DELETE", P.red, {
                onDelete(item)
                if (items.size <= 1) onClose() else index = index.coerceAtMost(items.size - 2)
            }, Modifier.width(110.dp), height = 40.dp)
            LcarsButton("CLOSE", P.peach, onClose, Modifier.width(100.dp), height = 40.dp)
            Spacer(Modifier.width(4.dp))
        }
    }
}

private fun decodeImage(file: File, maxSide: Int): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= 28) {
        // ImageDecoder applies EXIF rotation, so iPhone portrait photos stand upright.
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
            val s = info.size
            val scale = maxSide.toFloat() / maxOf(s.width, s.height)
            if (scale < 1f) decoder.setTargetSize((s.width * scale).toInt().coerceAtLeast(1), (s.height * scale).toInt().coerceAtLeast(1))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
        BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}.getOrNull()

private fun thumbnail(item: MediaItem, maxSide: Int): Bitmap? = when (item.kind) {
    MediaKind.IMAGE -> decodeImage(item.file, maxSide)
    MediaKind.VIDEO -> runCatching {
        MediaMetadataRetriever().run {
            try {
                setDataSource(item.file.path)
                getFrameAtTime(1_000_000)?.let { Bitmap.createScaledBitmap(it, maxSide, maxSide * it.height / it.width.coerceAtLeast(1), true) }
            } finally {
                release()
            }
        }
    }.getOrNull()
    else -> null
}
