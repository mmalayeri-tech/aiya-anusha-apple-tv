package app.aiya.tv

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.aiya.tv.data.Catalog
import app.aiya.tv.data.FoundApp
import app.aiya.tv.data.Inbox
import app.aiya.tv.data.Launcher
import app.aiya.tv.data.MediaItem
import app.aiya.tv.data.Net
import app.aiya.tv.data.Service
import app.aiya.tv.server.SendServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface Incoming {
    data class Media(val item: MediaItem) : Incoming
    data class Link(val url: String) : Incoming
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val launcher = Launcher(app)
    private val inbox = Inbox(File(app.filesDir, "inbox"))

    private val _media = MutableStateFlow(inbox.list())
    val media: StateFlow<List<MediaItem>> = _media

    private val _receivers = MutableStateFlow<List<FoundApp>>(emptyList())
    val receivers: StateFlow<List<FoundApp>> = _receivers

    /** Service id → installed package, for services with an Android app. */
    private val _installed = MutableStateFlow<Map<String, String>>(emptyMap())
    val installed: StateFlow<Map<String, String>> = _installed

    private val _sendUrl = MutableStateFlow<String?>(null)
    val sendUrl: StateFlow<String?> = _sendUrl

    private val _incoming = MutableSharedFlow<Incoming>(extraBufferCapacity = 8)
    val incoming: SharedFlow<Incoming> = _incoming

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    val tvName: String = "AIYA ANUSHA TV · ${Build.MODEL.uppercase()}"

    private val server = SendServer(
        inboxDir = inbox.dir,
        tvName = { tvName },
        onFile = { file ->
            refreshMedia()
            _incoming.tryEmit(Incoming.Media(MediaItem(file)))
        },
        onLink = { url -> _incoming.tryEmit(Incoming.Link(url)) },
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { server.start() }
            refreshAll()
        }
    }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _installed.value = Catalog.services.mapNotNull { s ->
                launcher.firstInstalled(s.packages)?.let { s.id to it }
            }.toMap()
            _receivers.value = launcher.findReceivers()
            _sendUrl.value = Net.localIpv4(getApplication())?.takeIf { server.port > 0 }?.let { "http://$it:${server.port}/" }
        }
    }

    fun refreshMedia() { _media.value = inbox.list() }

    fun clearMedia() {
        inbox.clear()
        refreshMedia()
        say("GALLERY CLEARED")
    }

    fun delete(item: MediaItem) {
        item.file.delete()
        refreshMedia()
    }

    /** Opens a service's app; returns the web URL to show in-app when there is no app. */
    fun openService(s: Service): String? {
        val pkg = _installed.value[s.id]
        if (pkg != null && launcher.launch(pkg)) return null
        return s.webUrl
    }

    fun getApp(s: Service) {
        val pkg = s.storePackage ?: return
        if (!launcher.openStore(pkg)) say("PLAY STORE NOT AVAILABLE")
    }

    fun openReceiver(app: FoundApp) {
        if (!launcher.launch(app.pkg)) say("COULD NOT OPEN ${app.label}")
    }

    fun installReceiver() {
        val pkg = Catalog.receivers.first().pkg
        if (!launcher.openStore(pkg) && !launcher.searchStore("airplay receiver")) say("PLAY STORE NOT AVAILABLE")
    }

    fun say(message: String) {
        _toast.value = message
        viewModelScope.launch {
            delay(2600)
            if (_toast.value == message) _toast.value = null
        }
    }

    override fun onCleared() {
        server.stop()
    }
}
