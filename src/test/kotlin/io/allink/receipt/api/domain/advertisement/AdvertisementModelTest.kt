package io.allink.receipt.api.domain.advertisement

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class AdvertisementModelTest {

  @Test
  fun `AdvertisementModel getters should return assigned values`() {
    val now = LocalDateTime.parse("2025-04-21T10:11:12")
    val mod = LocalDateTime.parse("2025-04-22T10:11:12")
    val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    val model = AdvertisementModel(
      merchantGroupId = "MG-001",
      title = "봄맞이 프로모션",
      regDate = now,
      modDate = mod,
      id = id
    )

    assertEquals("MG-001", model.merchantGroupId)
    assertEquals("봄맞이 프로모션", model.title)
    assertEquals(now, model.regDate)
    assertEquals(mod, model.modDate)
    assertEquals(id, model.id)
  }
}
