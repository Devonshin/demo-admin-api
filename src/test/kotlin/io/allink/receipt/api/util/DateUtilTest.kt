package io.allink.receipt.api.util

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DateUtilTest {

    @Test
    fun `zoneId should return system default zone`() {
        // Given & When
        val zoneId = DateUtil.zoneId

        // Then
        assertNotNull(zoneId)
        assertEquals(ZoneId.systemDefault(), zoneId)
    }

    @Test
    fun `nowLocalDateTime should return current LocalDateTime`() {
        // Given
        val before = LocalDateTime.now()

        // When
        val result = DateUtil.nowLocalDateTime()
        val after = LocalDateTime.now()

        // Then
        assertNotNull(result)
        assertTrue(result.isAfter(before.minusSeconds(1)))
        assertTrue(result.isBefore(after.plusSeconds(1)))
    }

    @Test
    fun `nowLocalDateTimeStr should return formatted string`() {
        // Given & When
        val result = DateUtil.nowLocalDateTimeStr()

        // Then
        assertNotNull(result)
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `nowLocalDateTimeStrMs should return formatted string with milliseconds`() {
        // Given & When
        val result = DateUtil.nowLocalDateTimeStrMs()

        // Then
        assertNotNull(result)
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")))
    }

    @Test
    fun `nowLocalDateTimeFormat should format given LocalDateTime`() {
        // Given
        val dateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45)

        // When
        val result = DateUtil.nowLocalDateTimeFormat(dateTime)

        // Then
        assertEquals("2023-12-25 15:30:45", result)
    }

    @Test
    fun `nowInstant with LocalDateTime should return correct Instant`() {
        // Given
        val dateTime = LocalDateTime.of(2023, 6, 15, 12, 0, 0)

        // When
        val result = DateUtil.nowInstant(dateTime)

        // Then
        assertNotNull(result)
        assertTrue(result.toString().contains("2023-06-15"))
    }

    @Test
    fun `nowInstant should return current Instant`() {
        // Given
        val before = System.currentTimeMillis()

        // When
        val result = DateUtil.nowInstant()
        val after = System.currentTimeMillis()

        // Then
        assertNotNull(result)
        val resultMillis = result.toEpochMilli()
        assertTrue(resultMillis >= before - 1000)
        assertTrue(resultMillis <= after + 1000)
    }

    @Test
    fun `date formatting should be consistent`() {
        // Given
        val dateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0)

        // When
        val formatted = DateUtil.nowLocalDateTimeFormat(dateTime)
        val expectedFormat = "2023-01-01 00:00:00"

        // Then
        assertEquals(expectedFormat, formatted)
    }

    @Test
    fun `should handle leap year dates`() {
        // Given
        val leapYearDate = LocalDateTime.of(2024, 2, 29, 10, 15, 30)

        // When
        val formatted = DateUtil.nowLocalDateTimeFormat(leapYearDate)

        // Then
        assertEquals("2024-02-29 10:15:30", formatted)
    }

    @Test
    fun `should handle edge case times`() {
        // Given
        val midnightDate = LocalDateTime.of(2023, 12, 31, 23, 59, 59)

        // When
        val formatted = DateUtil.nowLocalDateTimeFormat(midnightDate)

        // Then
        assertEquals("2023-12-31 23:59:59", formatted)
    }
}