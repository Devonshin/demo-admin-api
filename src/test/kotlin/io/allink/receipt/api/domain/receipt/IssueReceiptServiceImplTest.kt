package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.repository.TransactionUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @file IssueReceiptServiceImplTest.kt
 * @brief receipt 도메인의 Service 단위 테스트 - 트랜잭션 유틸 모킹 + Repository 상호작용 검증
 * @author Devonshin
 * @date 2025-09-12
 */
class IssueReceiptServiceImplTest {

  private val repository: IssueReceiptRepository = mockk(relaxed = true)
  private val service: IssueReceiptService = IssueReceiptServiceImpl(repository)

  @Test
  fun `Should return paged receipts`() = runBlocking {
    // given
    val filter = ReceiptFilter(
      period = PeriodFilter(LocalDateTime.parse("2025-06-01T00:00:00"), LocalDateTime.parse("2025-06-30T23:59:59")),
      phone = null, userId = null, userName = null, userNickName = null, tagUid = null, storeId = null, businessNo = null, storeName = "매장", franchiseCode = null,
      sort = listOf(Sorter("issueDate", "desc")),
      page = Page(1, 10)
    )
    val sample = SimpleIssueReceiptModel(
      id = "R-1",
      store = io.allink.receipt.api.domain.store.SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      tagId = "T-1",
      issueDate = LocalDateTime.parse("2025-06-02T00:00:00"),
      user = SimpleUserModel(id = "U-1", name = "홍길동"),
      receiptType = "PAY",
      receiptAmount = 1000,
      originIssueId = null
    )
    val pageResult = PagedResult(items = listOf(sample), totalCount = 1, currentPage = 1, totalPages = 1)

    coEvery { repository.findAll(filter) } returns pageResult

    mockkObject(TransactionUtil)
    TransactionUtil.init(mockk())

    coEvery { TransactionUtil.withTransaction<PagedResult<SimpleIssueReceiptModel>>(any(),any(),any()) } coAnswers {
      val block = arg<suspend () -> PagedResult<SimpleIssueReceiptModel>>(2)
      block.invoke()
    }

    // when
    val result = service.findAllReceipt(filter)

    // then
    assertEquals(1, result.totalCount)
    assertEquals("R-1", result.items.first().id)
    coVerify { repository.findAll(filter) }
  }

  @Test
  fun `Should return receipt by user and id`() = runBlocking {
    // given
    val model = IssueReceiptModel(
      id = "R-1",
      store = io.allink.receipt.api.domain.store.SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      tag = SimpleMerchantTagReceiptModel(id = "T-1", deviceId = "D-1"),
      issueDate = LocalDateTime.parse("2025-06-02T00:00:00"),
      user = SimpleUserModel(id = "U-1", name = "홍길동"),
      receiptType = "PAY",
      receiptAmount = 1000,
      originIssueId = null,
      userPointReview = null,
      edoc = null,
      advertisement = null
    )

    coEvery { repository.findByIdAndUserId("U-1", "R-1") } returns model

    mockkObject(TransactionUtil)
    TransactionUtil.init(mockk())
    coEvery { TransactionUtil.withTransaction<IssueReceiptModel?>(any(),any(), any()) } coAnswers {
      val block = arg<suspend () -> IssueReceiptModel?>(2)
      block.invoke()
    }

    // when
    val result = service.findReceipt("U-1", "R-1")

    // then
    assertEquals("R-1", result?.id)
    coVerify { repository.findByIdAndUserId("U-1", "R-1") }
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(TransactionUtil)
  }

  @Test
  fun `Should return empty page when no data`() = runBlocking {
    // given
    val filter = ReceiptFilter(
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = null, userId = null, userName = null, userNickName = null, tagUid = null, storeId = null, businessNo = null, storeName = null, franchiseCode = null,
      sort = listOf(Sorter("issueDate", "desc")),
      page = Page(1, 10)
    )

    coEvery { repository.findAll(filter) } returns PagedResult(emptyList(), totalCount = 0, currentPage = 1, totalPages = 0)

    mockkObject(TransactionUtil)
    TransactionUtil.init(mockk())
    coEvery { TransactionUtil.withTransaction<PagedResult<SimpleIssueReceiptModel>>(any(),any(),any()) } coAnswers {
      val block = arg<suspend () -> PagedResult<SimpleIssueReceiptModel>>(2)
      block.invoke()
    }

    // when
    val page = service.findAllReceipt(filter)

    // then
    assertEquals(0, page.totalCount)
    assertEquals(0, page.items.size)
    coVerify { repository.findAll(filter) }
  }

  @Test
  fun `Should return null when receipt not found`() = runBlocking {
    // given
    coEvery { repository.findByIdAndUserId("U-404", "R-404") } returns null

    mockkObject(TransactionUtil)

    coEvery { TransactionUtil.withTransaction<IssueReceiptModel?>(any(), any(), any()) } coAnswers {
      val block = arg<suspend () -> IssueReceiptModel?>(2)
      block.invoke()
    }

    // when
    val result = service.findReceipt("U-404", "R-404")

    // then
    assertEquals(null, result)
    coVerify { repository.findByIdAndUserId("U-404", "R-404") }
  }
}
