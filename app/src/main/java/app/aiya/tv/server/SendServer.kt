package app.aiya.tv.server

import android.util.Log
import app.aiya.tv.data.MediaItem
import app.aiya.tv.data.MediaKind
import java.io.BufferedInputStream
import java.io.File
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.Executors

/**
 * A tiny LAN web server. The iPhone opens it in Safari (via the QR code) and uploads photos,
 * videos or a link; the TV shows them. Uploads are streamed straight to disk.
 */
class SendServer(
    private val inboxDir: File,
    private val tvName: () -> String,
    private val onFile: (File) -> Unit,
    private val onLink: (String) -> Unit,
) {
    private var socket: ServerSocket? = null
    private val pool = Executors.newFixedThreadPool(4)
    var port: Int = 0
        private set

    fun start(preferredPort: Int = PORT) {
        if (socket != null) return
        val s = ServerSocket()
        s.reuseAddress = true
        try {
            s.bind(InetSocketAddress(preferredPort))
        } catch (e: Exception) {
            s.bind(InetSocketAddress(0)) // preferred port taken: any free one, the QR code follows
        }
        socket = s
        port = s.localPort
        Thread({ acceptLoop(s) }, "send-server").apply { isDaemon = true }.start()
    }

    fun stop() {
        runCatching { socket?.close() }
        socket = null
        pool.shutdownNow()
    }

    private fun acceptLoop(s: ServerSocket) {
        while (!s.isClosed) {
            val client = try { s.accept() } catch (_: Exception) { break }
            pool.execute { handle(client) }
        }
    }

    private fun handle(client: Socket): Unit = client.use { c ->
        try {
            c.soTimeout = 30_000
            val input = BufferedInputStream(c.getInputStream(), 64 * 1024)
            val out = c.getOutputStream()
            val req = Http.readRequest(input) ?: return
            val reply = when {
                req.method == "GET" && req.path == "/" ->
                    Http.response("200 OK", "text/html; charset=utf-8", UploadPage.html(tvName()).toByteArray())
                req.method == "POST" && req.path == "/upload" -> receiveFile(req, input)
                req.method == "POST" && req.path == "/open" -> receiveLink(req, input)
                else -> Http.response("404 Not Found", "text/plain", "not found".toByteArray())
            }
            out.write(reply)
            out.flush()
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "client timed out")
        } catch (e: Exception) {
            Log.w(TAG, "request failed", e)
        }
    }

    private fun receiveFile(req: Request, input: java.io.InputStream): ByteArray {
        val length = req.contentLength
        if (length <= 0 || length > MAX_UPLOAD) {
            return Http.response("413 Payload Too Large", "text/plain", "bad size".toByteArray())
        }
        val name = "${System.currentTimeMillis()}__${Http.safeFileName(req.header("x-filename"))}"
        if (MediaItem.kindOf(name) == MediaKind.OTHER) {
            return Http.response("415 Unsupported Media Type", "text/plain", "photos, videos and audio only".toByteArray())
        }
        val part = File(inboxDir, "$name.part")
        var remaining = length
        part.outputStream().buffered(256 * 1024).use { fileOut ->
            val buf = ByteArray(64 * 1024)
            while (remaining > 0) {
                val n = input.read(buf, 0, minOf(buf.size.toLong(), remaining).toInt())
                if (n < 0) break
                fileOut.write(buf, 0, n)
                remaining -= n
            }
        }
        if (remaining > 0) {
            part.delete()
            return Http.response("400 Bad Request", "text/plain", "incomplete".toByteArray())
        }
        val done = File(inboxDir, name)
        part.renameTo(done)
        onFile(done)
        return Http.response("200 OK", "text/plain", "ok".toByteArray())
    }

    private fun receiveLink(req: Request, input: java.io.InputStream): ByteArray {
        val length = req.contentLength
        if (length !in 1..4096) return Http.response("400 Bad Request", "text/plain", "bad".toByteArray())
        val body = ByteArray(length.toInt())
        var read = 0
        while (read < body.size) {
            val n = input.read(body, read, body.size - read)
            if (n < 0) break
            read += n
        }
        val url = Http.safeUrl(String(body, 0, read, Charsets.UTF_8))
            ?: return Http.response("400 Bad Request", "text/plain", "not a link".toByteArray())
        onLink(url)
        return Http.response("200 OK", "text/plain", "ok".toByteArray())
    }

    companion object {
        const val PORT = 8765
        private const val MAX_UPLOAD = 4L * 1024 * 1024 * 1024
        private const val TAG = "SendServer"
    }
}
