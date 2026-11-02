package app.aiya.tv.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

data class FoundApp(val label: String, val pkg: String)

/** Opening other apps and the Play Store. Never throws: returns false when nothing could open. */
class Launcher(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    fun isInstalled(pkg: String): Boolean = launchIntent(pkg) != null

    fun firstInstalled(pkgs: List<String>): String? = pkgs.firstOrNull { isInstalled(it) }

    fun launch(pkg: String): Boolean = launchIntent(pkg)?.let { start(it) } ?: false

    fun openStore(pkg: String): Boolean =
        start(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))) ||
            start(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))

    fun searchStore(query: String): Boolean =
        start(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(query)}")))

    /** Hands a link to whatever app claims it (YouTube, Apple TV, …). False if none does. */
    fun openExternally(url: String): Boolean = start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    /** Installed apps that look like AirPlay receivers, known ones first. */
    fun findReceivers(): List<FoundApp> {
        val found = linkedMapOf<String, FoundApp>()
        Catalog.receivers.forEach { r -> if (isInstalled(r.pkg)) found[r.pkg] = FoundApp(r.title, r.pkg) }
        for (category in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            for (info in pm.queryIntentActivities(query, 0)) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName || pkg in found) continue
                val label = info.loadLabel(pm).toString()
                if (Catalog.looksLikeReceiver(label, pkg)) found[pkg] = FoundApp(label.uppercase(), pkg)
            }
        }
        return found.values.toList()
    }

    private fun launchIntent(pkg: String): Intent? =
        pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)

    private fun start(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
