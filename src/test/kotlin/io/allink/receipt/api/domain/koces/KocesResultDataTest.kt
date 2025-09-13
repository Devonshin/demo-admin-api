package io.allink.receipt.api.domain.koces

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class KocesResultDataTest {

  @Test
  fun `KocesResultData getters should return assigned values`() {
    val data = KocesResultData(responseSeq = 123L, message = "OK")
    assertEquals(123L, data.responseSeq)
    assertEquals("OK", data.message)
  }

  @Test
  fun `KocesResultData should encode and decode via Json`() {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val original = KocesResultData(responseSeq = 999L, message = "success")

    val encoded = json.encodeToString(original)
    val decoded = json.decodeFromString<KocesResultData>(encoded)

    assertEquals(original, decoded)
  }
}
