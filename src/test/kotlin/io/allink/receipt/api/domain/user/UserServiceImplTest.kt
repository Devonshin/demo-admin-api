package io.allink.receipt.api.domain.user

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
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

/**
 * @file UserServiceImplTest.kt
 * @brief user 도메인의 Service 단위 테스트 - 트랜잭션 유틸을 모킹하여 Repository 상호작용 검증
 * @author Devonshin
 * @date 2025-09-12
 */
class UserServiceImplTest {

  private val repository: UserRepository = mockk(relaxed = true)
  private val service: UserService = UserServiceImpl(repository)

  @Test
  fun `Should return user when find user`() = runBlocking {
    // given
    val user = UserModel(
      id = "u-1",
      name = "홍길동",
      status = UserStatus.ACTIVE,
      phone = "01012345678",
      gender = "M",
      ci = null,
      birthday = "1990-01-01",
      localYn = "Y",
      email = "test@example.com",
      role = UserRole.USER,
      joinSocialType = "NAVER",
      nickname = "길동",
      mtchgId = null,
      cpointRegType = null,
      cpointRegDate = null,
      regDate = null,
      modDate = null
    )

    coEvery { repository.find("u-1") } returns user

    // TransactionUtil.withTransaction 을 실제 DB 없이 동작시키도록 모킹
    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction<UserModel?>(any()) } coAnswers {
      val block = arg<suspend () -> UserModel?>(0)
      block.invoke()
    }

    // when
    val result = service.findUser("u-1")

    // then
    assertEquals(user, result)
    coVerify { repository.find("u-1") }
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(TransactionUtil)
  }

  @Test
  fun `Should Return Paged Users When Find All User`() = runBlocking {
    // given
    val filter = UserFilter(
      phone = null,
      name = "홍",
      nickName = null,
      age = null,
      gender = null,
      sort = listOf(Sorter(field = "name", direction = "asc")),
      page = Page(page = 1, pageSize = 10)
    )

    val users = listOf(
      UserModel("u-1", "홍길동", UserStatus.ACTIVE, "010", "M", null, "1990", "Y", "a@b.c", UserRole.USER, "NAVER", "길동", null, null, null, null, null),
      UserModel("u-2", "홍길서", UserStatus.NORMAL, "010", "F", null, "1991", "Y", "a@b.c", UserRole.USER, "KAKAO", "길서", null, null, null, null, null)
    )

    val paged = PagedResult(
      items = users,
      totalCount = 2,
      currentPage = 1,
      totalPages = 1
    )

    coEvery { repository.findAll(filter) } returns paged

    // 트랜잭션 유틸 모킹
    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction<PagedResult<UserModel>>(any()) } coAnswers {
      val block = arg<suspend () -> PagedResult<UserModel>>(0)
      block.invoke()
    }

    // when
    val result = service.findAllUser(filter)

    // then
    assertEquals(2, result.totalCount)
    assertEquals(1, result.currentPage)
    assertEquals(1, result.totalPages)
    assertEquals("홍길동", result.items.first().name)
    coVerify { repository.findAll(filter) }
  }
}