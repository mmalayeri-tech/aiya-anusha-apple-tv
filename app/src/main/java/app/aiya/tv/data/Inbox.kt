package app.aiya.tv.data

import java.io.File

enum class MediaKind { IMAGE, VIDEO, AUDIO, OTHER }

data class MediaItem(val file: File) {
    val name: String get() = file.name.substringAfter("__")
    val kind: MediaKind get() = kindOf(file.name)

    companion object {
        fun kindOf(name: String): MediaKind = when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp" -> MediaKind.IMAGE
            "mp4", "mov", "m4v", "3gp", "mkv", "webm" -> MediaKind.VIDEO
            "mp3", "m4a", "aac", "wav", "flac", "ogg" -> MediaKind.AUDIO
            else -> MediaKind.OTHER
        }
    }
}

/** Files sent from the iPhone, newest first. Names are "<millis>__<original name>". */
class Inbox(val dir: File) {
    init { dir.mkdirs() }

    fun list(): List<MediaItem> =
        (dir.listFiles() ?: emptyArray()).filter { it.isFile && !it.name.endsWith(".part") }
            .sortedByDescending { it.lastModified() }
            .map(::MediaItem)

    fun clear() { dir.listFiles()?.forEach { it.delete() } }
}
