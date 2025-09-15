package io.allink.receipt.api.domain.login

import io.allink.receipt.api.domain.admin.*
import io.allink.receipt.api.domain.sns.VerificationService
import io.allink.receipt.api.repository.TransactionUtil
import io.ktor.server.config.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * @file LoginServiceImplTest.kt
 * @brief login 도메인의 Service 단위 테스트(인증코드 생성/검증)
 * @author Devonshin
 * @date 2025-09-12
 */
class LoginServiceImplTest {

  private val loginInfoRepository: LoginInfoRepository = mockk(relaxed = true)
  private val adminService: AdminService = mockk(relaxed = true)
  private val verificationService: VerificationService = mockk(relaxed = true)
  private val config: ApplicationConfig = MapApplicationConfig(
    "jwt.expiresIn" to "3600",
    "jwt.audience" to "test-aud",
    "jwt.issuer" to "test-iss",
    "jwt.secret" to "test-secret"
  )

  private val jwtGenerator: JwtGenerator = mockk(relaxed = true)

  private val service: LoginService = LoginServiceImpl(
    loginInfoRepository, adminService, verificationService, config, jwtGenerator
  )

  @BeforeEach
  fun setUp() {
    // TransactionUtil.withTransaction(...) 내부에서 R2DBC TransactionManager를 요구하지 않도록 차단
    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction(any<suspend () -> VerificationCode>()) } coAnswers {
      firstArg<suspend () -> VerificationCode>().invoke()
    }
    coEvery { TransactionUtil.withTransaction(any<suspend () -> Jwt>()) } coAnswers {
      firstArg<suspend () -> Jwt>().invoke()
    }
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(TransactionUtil)
  }

  @Test
  fun `Should generate verification code for existing admin`() = runBlocking {
    // given
    val adminId = UUID.randomUUID()
    val admin = AdminModel(
      id = adminId,
      loginId = null,
      password = null,
      fullName = "관리자",
      role = MasterRole(),
      phone = "01000000000",
      email = null,
      status = AdminStatus.ACTIVE
    )

    coEvery { adminService.findByPhoneNo("01000000000") } returns admin
    coEvery { loginInfoRepository.create(any()) } answers {
      val arg = firstArg<LoginInfoModel>()
      arg.copy(id = UUID.randomUUID())
    }

    // when
    val result = service.generateVerificationCode(VerificationCodeRequest(phone = "01000000000"))

    // then
    assertNotNull(result.loginUuid)
    coVerify { verificationService.sendVerificationMessage("01000000000", any(), any()) }
  }

  @Test
  fun `Should check verification code and return jwt`() = runBlocking {
    // given
    val loginUuid = UUID.randomUUID()
    val adminId = UUID.randomUUID()
    val admin = AdminModel(
      id = adminId,
      loginId = null,
      password = null,
      fullName = "관리자",
      role = MasterRole(),
      phone = "01000000000",
      email = null,
      status = AdminStatus.ACTIVE
    )

    val loginInfo = LoginInfoModel(
      id = loginUuid,
      userUuid = adminId,
      verificationCode = "123456",
      expireDate = LocalDateTime.now().plusMinutes(5),
      status = LoginStatus.PENDING,
      loginDate = null
    )

    coEvery { loginInfoRepository.find(loginUuid) } returns loginInfo
    coEvery { adminService.findByUserUuId(adminId) } returns admin
    coEvery { loginInfoRepository.update(any()) } returns 1
    coEvery { jwtGenerator.fromLogin(any(), any(), any()) } returns Jwt(
      jwt = "token",
      expireDate = "2025-12-31 23:59:59",
      username = "관리자",
      role = MasterRole()
    )

    // when
    val jwt = service.checkVerificationCode(
      VerificationCheckRequest(loginUuid = loginUuid.toString(), verificationCode = "123456")
    )

    // then
    assertNotNull(jwt.jwt)
    assertEquals("관리자", jwt.username)
    assertEquals("MASTER", jwt.role.toRoleString())
  }
}