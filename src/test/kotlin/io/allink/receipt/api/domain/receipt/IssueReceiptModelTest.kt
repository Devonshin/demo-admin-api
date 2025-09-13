package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.store.SimpleStoreModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @file IssueReceiptModelTest.kt
 * @brief receipt 도메인 모델 단위 테스트 (단순 생성/edoc 연결 검증)
 * @author Devonshin
 * @date 2025-09-12
 */
class IssueReceiptModelTest {

  @Test
  fun `should_create_simple_issue_receipt_model`() {
    val model = SimpleIssueReceiptModel(
      id = "R-1",
      store = SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      tagId = "T-1",
      issueDate = LocalDateTime.parse("2025-06-02T00:00:00"),
      user = SimpleUserModel(id = "U-1", name = "홍길동"),
      receiptType = "PAY",
      receiptAmount = 1000,
      originIssueId = null
    )

    assertEquals("R-1", model.id)
    assertEquals("S-1", model.store.id)
    assertEquals("T-1", model.tagId)
    assertEquals("U-1", model.user.id)
    assertEquals("PAY", model.receiptType)
    assertEquals(1000, model.receiptAmount)
  }

  @Test
  fun `should_attach_edoc_to_issue_receipt_model`() {
    val issue = IssueReceiptModel(
      id = "R-2",
      store = SimpleStoreModel(id = "S-2", storeName = "상점", franchiseCode = null, businessNo = null, ceoName = null),
      tag = SimpleMerchantTagReceiptModel(id = "T-2", deviceId = "D-2"),
      issueDate = LocalDateTime.parse("2025-06-03T00:00:00"),
      user = SimpleUserModel(id = "U-2", name = "이몽룡"),
      receiptType = "REFUND",
      receiptAmount = 2000,
      originIssueId = null,
      userPointReview = null,
      edoc = null,
      advertisement = null
    )

    val edoc = SimpleEdocModel(
      id = "kakao",
      envelopId = "ENV-123",
      regDate = LocalDateTime.parse("2025-06-03T01:00:00")
    )

    issue.edoc = edoc

    assertNotNull(issue.edoc)
    assertEquals("kakao", issue.edoc?.id)
    assertEquals("ENV-123", issue.edoc?.envelopId)
  }
}