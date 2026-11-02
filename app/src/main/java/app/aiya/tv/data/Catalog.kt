package app.aiya.tv.data

/**
 * Apple services that can be reached from an Android / Google TV box.
 * [packages] are tried in order; the first installed one is launched. When none is installed,
 * [storePackage] is offered on the Play Store and [webUrl] opens in the built-in web screen.
 */
data class Service(
    val id: String,
    val title: String,
    val subtitle: String,
    val packages: List<String>,
    val storePackage: String?,
    val webUrl: String?,
)

/** A known AirPlay receiver app. Any other installed app whose name mentions AirPlay is found too. */
data class Receiver(val title: String, val pkg: String, val note: String)

object Catalog {
    val services = listOf(
        Service(
            id = "tv",
            title = "APPLE TV+",
            subtitle = "SHOWS · MOVIES · MLS",
            packages = listOf("com.apple.atve.androidtv.appletv", "com.apple.atve.amazon.appletv"),
            storePackage = "com.apple.atve.androidtv.appletv",
            webUrl = "https://tv.apple.com",
        ),
        Service(
            id = "music",
            title = "APPLE MUSIC",
            subtitle = "SONGS · RADIO · LYRICS",
            packages = listOf("com.apple.android.music"),
            storePackage = "com.apple.android.music",
            webUrl = "https://music.apple.com",
        ),
        Service(
            id = "podcasts",
            title = "PODCASTS",
            subtitle = "APPLE PODCASTS ON THE WEB",
            packages = emptyList(),
            storePackage = null,
            webUrl = "https://podcasts.apple.com",
        ),
        Service(
            id = "icloud",
            title = "ICLOUD PHOTOS",
            subtitle = "SIGN IN ON THE WEB",
            packages = emptyList(),
            storePackage = null,
            webUrl = "https://www.icloud.com/photos",
        ),
        Service(
            id = "trailers",
            title = "TRAILERS",
            subtitle = "NEW ON APPLE TV+",
            packages = emptyList(),
            storePackage = null,
            webUrl = "https://tv.apple.com/channel/tvs.sbd.4000",
        ),
    )

    val receivers = listOf(
        Receiver("AIRSCREEN", "com.ionitech.airscreen", "FREE · AIRPLAY + SCREEN MIRRORING"),
    )

    /** Words that mark an installed app as an AirPlay receiver. */
    val receiverKeywords = listOf("airplay", "airscreen", "airreceiver", "air receiver", "mirror")

    fun looksLikeReceiver(label: String, pkg: String): Boolean {
        val l = label.lowercase()
        return receivers.any { it.pkg == pkg } || receiverKeywords.any { l.contains(it) }
    }
}
