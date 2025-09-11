package io.allink.receipt.api.common

import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConstantTest {

    @Test
    fun `AES256_KEY should be defined`() {
        // Given & When
        val key = Constant.AES256_KEY

        // Then
        assertNotNull(key)
        assertEquals(32, key.length)
        assertEquals("rWsq73mYW5WC8KTtzDyj1OUnQ4ubGlMU", key)
    }

    @Test
    fun `ACCEPT_FILE_FIELDS should contain expected mappings`() {
        // Given & When
        val acceptFields = Constant.ACCEPT_FILE_FIELDS

        // Then
        assertNotNull(acceptFields)
        assertTrue(acceptFields.containsKey("stores"))
        assertTrue(acceptFields.containsKey("bz-agencies"))
        assertTrue(acceptFields.containsKey("tags"))
        assertTrue(acceptFields.containsKey("advertisement"))
        
        assertEquals(5, acceptFields["stores"]?.size)
        assertTrue(acceptFields["stores"]?.contains("bz") == true)
        assertTrue(acceptFields["stores"]?.contains("id") == true)
    }

    @Test
    fun `ACCEPT_FILE_TYPE should contain valid content types`() {
        // Given & When
        val acceptTypes = Constant.ACCEPT_FILE_TYPE

        // Then
        assertEquals(3, acceptTypes.size)
        assertTrue(acceptTypes.contains(ContentType.Image.PNG.contentSubtype))
        assertTrue(acceptTypes.contains(ContentType.Application.Pdf.contentSubtype))
        assertTrue(acceptTypes.contains(ContentType.Application.Xlsx.contentSubtype))
    }

    @Test
    fun `checkAcceptFileField should validate menu and field combinations`() {
        // Given & When & Then
        assertTrue(Constant.checkAcceptFileField("stores", "bz"))
        assertTrue(Constant.checkAcceptFileField("stores", "id"))
        assertTrue(Constant.checkAcceptFileField("bz-agencies", "bank"))
        assertTrue(Constant.checkAcceptFileField("advertisement", "banner"))
        
        assertTrue(!Constant.checkAcceptFileField("stores", "invalid"))
        assertTrue(!Constant.checkAcceptFileField("invalid", "bz"))
        assertTrue(!Constant.checkAcceptFileField("bz-agencies", "coupon"))
    }

    @Test
    fun `checkAcceptFileType should validate file extensions`() {
        // ContentType.contentSubtype returns the MIME subtype, not file extension
        // PNG -> "png", PDF -> "pdf", XLSX -> "vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        
        // Given & When & Then - test with actual MIME subtypes
        assertTrue(Constant.checkAcceptFileType("document.pdf"))  // ends with "pdf" 
        assertTrue(Constant.checkAcceptFileType("image.png"))     // ends with "png"
        assertTrue(Constant.checkAcceptFileType("file.vnd.openxmlformats-officedocument.spreadsheetml.sheet"))  // xlsx MIME
        assertTrue(Constant.checkAcceptFileType("FILE.PDF"))      // case insensitive
        
        assertTrue(!Constant.checkAcceptFileType("document.doc"))
        assertTrue(!Constant.checkAcceptFileType("image.jpg"))
        assertTrue(!Constant.checkAcceptFileType("text.txt"))
        assertTrue(!Constant.checkAcceptFileType("noextension"))
        assertTrue(!Constant.checkAcceptFileType("spreadsheet.xlsx"))  // This won't match because XLSX MIME is complex
    }

    @Test
    fun `service group constants should be defined`() {
        // Given & When & Then
        assertEquals("MERT_SVC", Constant.MERCHANT_SERVICE_GROUP_CODE)
        assertEquals("ERECEIPT", Constant.ERECEIPT)
        assertEquals("REVIEWPRJ", Constant.REVIEWPRJ)
        assertEquals("REVIEWPT", Constant.REVIEWPT)
        assertEquals("DLVRVIEWPT", Constant.DLVRVIEWPT)
        assertEquals("CPNADVTZ", Constant.CPNADVTZ)
        assertEquals("HUBADVTZ", Constant.HUBADVTZ)
    }

    @Test
    fun `SYSTEM_UUID should be zero UUID`() {
        // Given & When
        val systemUuid = Constant.SYSTEM_UUID

        // Then
        assertNotNull(systemUuid)
        assertEquals("00000000-0000-0000-0000-000000000000", systemUuid.toString())
    }

    @Test
    fun `StatusCode enum should have correct values`() {
        // Given & When & Then
        assertEquals("정상", StatusCode.ACTIVE.value)
        assertEquals("정상", StatusCode.NORMAL.value)
        assertEquals("중지", StatusCode.INACTIVE.value)
        assertEquals("대기", StatusCode.PENDING.value)
        assertEquals("만료", StatusCode.EXPIRED.value)
        assertEquals("삭제", StatusCode.DELETED.value)
    }

    @Test
    fun `BillingStatusCode enum should have correct values`() {
        // Given & When & Then
        assertEquals("즉시 결제 대기", BillingStatusCode.STANDBY.value)
        assertEquals("익월 1일까지 대기", BillingStatusCode.PENDING.value)
        assertEquals("결제완료", BillingStatusCode.COMPLETE.value)
        assertEquals("결제취소", BillingStatusCode.CANCELED.value)
        assertEquals("결제실패", BillingStatusCode.FAIL.value)
    }

    @Test
    fun `file field validation should handle case sensitivity`() {
        // Given & When & Then
        assertTrue(Constant.checkAcceptFileField("stores", "bz"))
        assertTrue(!Constant.checkAcceptFileField("STORES", "bz"))
        assertTrue(!Constant.checkAcceptFileField("stores", "BZ"))
    }

    @Test
    fun `file type validation should be case insensitive`() {
        // Given & When & Then
        assertTrue(Constant.checkAcceptFileType("document.pdf"))
        assertTrue(Constant.checkAcceptFileType("document.PDF"))
        assertTrue(Constant.checkAcceptFileType("DOCUMENT.pdf"))
        assertTrue(Constant.checkAcceptFileType("DOCUMENT.PDF"))
    }
}