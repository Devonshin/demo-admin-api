package io.allink.receipt.api.domain.merchant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @file MerchantTagModelTest.kt
 * @brief merchant 도메인의 Model/매핑 기초 검증 (간단 값 확인)
 * @author Devonshin
 * @date 2025-09-12
 */
class MerchantTagModelTest {

  @Test
  fun `should_hold_simple_fields`() {
    val now = LocalDateTime.parse("2025-06-01T12:00:00")
    val model = MerchantTagModel(
      id = "T-1",
      store = null,
      merchantGroupId = "EDIYA",
      merchantStoreId = "M-1",
      tagName = "포스태그",
      deviceId = "D-1",
      storeUid = "S-1",
      regDate = now,
      modDate = null
    )
    assertEquals("T-1", model.id)
    assertEquals("EDIYA", model.merchantGroupId)
    assertEquals("포스태그", model.tagName)
  }
}