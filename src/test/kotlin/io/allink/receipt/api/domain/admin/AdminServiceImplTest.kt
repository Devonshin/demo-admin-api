package io.allink.receipt.api.domain.admin

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * @file AdminServiceImplTest.kt
 * @brief admin 도메인 Service 단위 테스트 - Repository 의존 모킹 및 위임 검증
 */
class AdminServiceImplTest {

  private val repository: AdminRepository = mockk(relaxed = true)
  private val service: AdminService = AdminServiceImpl(repository)

  @Test
  fun `should_find_by_phone_and_userUuid`() = runBlocking {
    // given
    val adminId = UUID.randomUUID()
    val model = AdminModel(
      id = adminId,
      loginId = "admin",
      password = null,
      fullName = "관리자",
      role = MasterRole(),
      phone = "01011112222",
      email = null,
      status = AdminStatus.ACTIVE
    )

    coEvery { repository.findByPhone("01011112222") } returns model
    coEvery { repository.findByUserUuid(adminId) } returns model

    // when
    val byPhone = service.findByPhoneNo("01011112222")
    val byUser = service.findByUserUuId(adminId)

    // then
    assertEquals("관리자", byPhone?.fullName)
    assertEquals(adminId, byUser?.id)

    coVerify { repository.findByPhone("01011112222") }
    coVerify { repository.findByUserUuid(adminId) }
  }
}