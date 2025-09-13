package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.repository.TransactionUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @file NPointServiceImplTest.kt
 * @brief npoint 도메인 Service 단위 테스트 - 트랜잭션 유틸 모킹 + Repository 상호작용 검증
 * @author Devonshin
 * @date 2025-09-12
 */
class NPointServiceImplTest {

  private val repository: NPointRepository = mockk(relaxed = true)
  private val service: NPointService = NPointServiceImpl(repository)

  @AfterEach
  fun tearDown() { unmockkObject(TransactionUtil) }

  @Test
  fun `should_return_paged_npoint_pays`() = runBlocking {
    // given
    val filter = NPointFilter(
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      page = Page(1, 10)
    )

    val item = NPointPayModel(
      id = 1L,
      point = 100,
      status = "지급완료",
      user = NPointUserModel(id = "U-1", name = "홍길동", phone = "010", gender = "M", birthday = "1990", nickname = "길동"),
      store = io.allink.receipt.api.domain.store.SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      provideCase = "이벤트",
      pointTrNo = "TR-1",
      pointPayNo = "PAY-1",
      regDate = LocalDateTime.parse("2025-06-02T00:00:00")
    )
    val page = PagedResult(items = listOf(item), totalCount = 1, currentPage = 1, totalPages = 1)

    coEvery { repository.findAll(filter) } returns page

    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction<PagedResult<NPointPayModel>>(any()) } coAnswers {
      val block = arg<suspend () -> PagedResult<NPointPayModel>>(0)
      block.invoke()
    }

    // when
    val result = service.getAllNPointPay(filter)

    // then
    assertEquals(1, result.totalCount)
    coVerify { repository.findAll(filter) }
  }
}