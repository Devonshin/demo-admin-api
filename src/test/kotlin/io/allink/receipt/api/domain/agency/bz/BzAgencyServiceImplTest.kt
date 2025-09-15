package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.TransactionUtil
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * @file BzAgencyServiceImplTest.kt
 * @brief bz agency 도메인 Service 단위 테스트 - 필터/상세 조회/예외 케이스
 */
class BzAgencyServiceImplTest {

  private val repository: BzAgencyRepository = mockk(relaxed = true)
  private val adminRepository: io.allink.receipt.api.domain.admin.AdminRepository = mockk(relaxed = true)
  private val service: BzAgencyService = BzAgencyServiceImpl(repository, adminRepository)

  @AfterEach
  fun tearDown() { unmockkObject(TransactionUtil) }

  @Test
  fun `Should return paged agencies`() = runBlocking {
    val filter = BzAgencyFilter(
      agencyName = "대리",
      sort = null
    )
    val item = SimpleBzAgencyModel(
      id = UUID.randomUUID(),
      agencyName = "대리점A",
      businessNo = "111-22-33333",
      status = AgencyStatus.ACTIVE,
      latestLoginAt = LocalDateTime.parse("2025-06-02T00:00:00")
    )
    coEvery { repository.findAllByFilter(filter) } returns PagedResult(listOf(item), 1, 1, 1)

    mockkObject(TransactionUtil)
    TransactionUtil.init(mockk())
    coEvery { TransactionUtil.withTransaction<PagedResult<SimpleBzAgencyModel>>(any(), any(), any()) } coAnswers {
      val block = arg<suspend () -> PagedResult<SimpleBzAgencyModel>>(2)
      block.invoke()
    }

    val page = service.getAgencies(filter)
    assertEquals(1, page.totalCount)
  }

  @Test
  fun `Should get agency detail and throw for invalid id`() = runBlocking {
    val id = UUID.randomUUID()
    val model = BzAgencyModel(id = id, agencyName = "대리점", businessNo = "1", status = AgencyStatus.ACTIVE)
    coEvery { repository.find(id) } returns model

    mockkObject(TransactionUtil)
    TransactionUtil.init(mockk())
    coEvery { TransactionUtil.withTransaction<BzAgencyModel>(any(), any(), any()) } coAnswers {
      val block = arg<suspend () -> BzAgencyModel>(2)
      block.invoke()
    }

    val detail = service.getAgency(id.toString())
    assertEquals("대리점", detail.agencyName)

    // invalid UUID
    assertThrows(io.ktor.server.plugins.BadRequestException::class.java) {
      runBlocking { service.getAgency("not-uuid") }
    }
  }
}