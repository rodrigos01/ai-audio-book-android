package com.rodrigos01.aiaudiobook.common.util

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream

class FileUtilsTest {

    @Test
    fun testReadPlainTextFromStream() {
        val sampleText = "Chapter 1\nThis is a test chapter content with utf-8 chars: é, ñ, 🚀"
        val inputStream = ByteArrayInputStream(sampleText.toByteArray(Charsets.UTF_8))
        val result = FileUtils.readTextFromStream(inputStream)
        assertEquals(sampleText, result)
    }

    @Test
    fun testCleanFileName() {
        assertEquals("Chapter 1", FileUtils.cleanFileName("Chapter 1.txt"))
        assertEquals("My Document", FileUtils.cleanFileName("My Document.pdf"))
        assertEquals("My Novel Part 2", FileUtils.cleanFileName("My Novel Part 2.TXT"))
        assertEquals("Story.draft", FileUtils.cleanFileName("Story.draft.txt"))
        assertEquals("Untitled", FileUtils.cleanFileName("Untitled"))
        assertNull(FileUtils.cleanFileName(""))
        assertNull(FileUtils.cleanFileName("   "))
        assertNull(FileUtils.cleanFileName(null))
    }

    @Test
    fun testExtractGoogleDocIdFromUrl() {
        val url = "https://docs.google.com/document/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit"
        val docId = Regex("/document/d/([a-zA-Z0-9_-]+)").find(url)?.groupValues?.get(1)
        assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", docId)
    }
}
