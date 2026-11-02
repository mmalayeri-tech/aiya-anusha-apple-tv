package app.aiya.tv

import app.aiya.tv.data.Catalog
import app.aiya.tv.data.MediaItem
import app.aiya.tv.data.MediaKind
import app.aiya.tv.server.Http
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpTest {
    @Test fun parsesRequest() {
        val r = Http.readRequest("POST /upload?x=1 HTTP/1.1\r\nContent-Length: 42\r\nX-Filename: a%20b.jpg\r\n\r\nBODY".byteInputStream())!!
        assertEquals("POST", r.method)
        assertEquals("/upload", r.path)
        assertEquals(42L, r.contentLength)
        assertEquals("a%20b.jpg", r.header("X-FILENAME"))
    }

    @Test fun rejectsTruncatedHead() {
        assertNull(Http.readRequest("GET / HTTP/1.1\r\n".byteInputStream()))
    }

    @Test fun sanitizesFileNames() {
        assertEquals("a b.jpg", Http.safeFileName("a%20b.jpg"))
        assertEquals("passwd", Http.safeFileName("..%2F..%2Fetc%2Fpasswd"))
        assertEquals("upload", Http.safeFileName(null))
        assertEquals("upload", Http.safeFileName("...."))
        assertEquals("IMG_0001_.HEIC", Http.safeFileName("IMG_0001?.HEIC"))
    }

    @Test fun acceptsOnlyWebLinks() {
        assertEquals("https://youtu.be/x", Http.safeUrl(" https://youtu.be/x "))
        assertEquals("https://apple.com", Http.safeUrl("apple.com"))
        assertNull(Http.safeUrl("javascript:alert(1)"))
        assertNull(Http.safeUrl("file:///etc/hosts"))
    }

    @Test fun mediaKinds() {
        assertEquals(MediaKind.IMAGE, MediaItem.kindOf("1__x.JPG"))
        assertEquals(MediaKind.VIDEO, MediaItem.kindOf("1__clip.MOV"))
        assertEquals(MediaKind.OTHER, MediaItem.kindOf("1__notes.txt"))
    }

    @Test fun receiverDetection() {
        assertTrue(Catalog.looksLikeReceiver("AirScreen", "com.ionitech.airscreen"))
        assertTrue(Catalog.looksLikeReceiver("AirPlay Receiver", "x.y"))
        assertFalse(Catalog.looksLikeReceiver("YouTube", "com.google.android.youtube.tv"))
    }
}
