package io.allink.receipt.api.domain.advertisement

import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer
import io.allink.receipt.api.config.plugin.UUIDSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class AdvertisementModelSerializationTest {

  @Test
  fun `AdvertisementModel should roundtrip encode decode`() {
    val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val model = AdvertisementModel(
      merchantGroupId = "MG-100",
      title = "여름 빅세일",
      regDate = now,
      modDate = now,
      id = id
    )

    val json = Json {
      serializersModule = SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
        contextual(UUID::class, UUIDSerializer)
      }
      encodeDefaults = true
      ignoreUnknownKeys = true
    }

    val encoded = json.encodeToString(model)
    val decoded = json.decodeFromString<AdvertisementModel>(encoded)

    assertEquals(model, decoded)
  }
}