package app.aiya.tv.server

import java.io.InputStream
import java.net.URLDecoder

data class Request(val method: String, val path: String, val headers: Map<String, String>) {
    fun header(name: String): String? = headers[name.lowercase()]
    val contentLength: Long get() = header("content-length")?.toLongOrNull() ?: -1
}

/** Minimal HTTP/1.1 helpers. Pure functions so they can be unit-tested off-device. */
object Http {
    private const val MAX_HEADER_BYTES = 16 * 1024

    /** Reads the request line and headers, leaving the stream positioned at the body. */
    fun readRequest(input: InputStream): Request? {
        val bytes = java.io.ByteArrayOutputStream()
        var last4 = 0
        while (true) {
            val b = input.read()
            if (b < 0) return null
            bytes.write(b)
            last4 = (last4 shl 8) or b
            if (last4 == 0x0D0A0D0A) break
            if (bytes.size() > MAX_HEADER_BYTES) return null
        }
        return parseHead(bytes.toString(Charsets.ISO_8859_1.name()))
    }

    fun parseHead(head: String): Request? {
        val lines = head.split("\r\n").filter { it.isNotEmpty() }
        val parts = lines.firstOrNull()?.split(' ') ?: return null
        if (parts.size < 2) return null
        val headers = lines.drop(1).mapNotNull { line ->
            val i = line.indexOf(':')
            if (i <= 0) null else line.substring(0, i).trim().lowercase() to line.substring(i + 1).trim()
        }.toMap()
        return Request(parts[0].uppercase(), parts[1].substringBefore('?'), headers)
    }

    /** Turns a client-supplied (URL-encoded) file name into a safe, flat one. */
    fun safeFileName(raw: String?): String {
        val decoded = runCatching { URLDecoder.decode(raw ?: "", "UTF-8") }.getOrDefault(raw ?: "")
        val base = decoded.substringAfterLast('/').substringAfterLast('\\')
        val cleaned = base.replace(Regex("[^A-Za-z0-9._ -]"), "_").trim().trimStart('.')
        return cleaned.take(120).ifEmpty { "upload" }
    }

    /** Accepts only http(s) links for "open on TV". */
    fun safeUrl(raw: String): String? {
        val u = raw.trim()
        val withScheme = if (u.startsWith("http://") || u.startsWith("https://")) u else if (u.contains('.') && !u.contains(' ')) "https://$u" else return null
        return withScheme.takeIf { it.length <= 2048 }
    }

    fun response(status: String, contentType: String, body: ByteArray): ByteArray {
        val head = "HTTP/1.1 $status\r\nContent-Type: $contentType\r\nContent-Length: ${body.size}\r\n" +
            "Cache-Control: no-store\r\nConnection: close\r\n\r\n"
        return head.toByteArray(Charsets.ISO_8859_1) + body
    }
}
