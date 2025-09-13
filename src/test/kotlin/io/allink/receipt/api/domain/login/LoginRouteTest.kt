package io.allink.receipt.api.domain.login

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.github.smiley4.ktoropenapi.OpenApi
import io.allink.receipt.api.config.plugin.configureSerialization
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import io.allink.receipt.api.testsupport.TestAuth
import io.allink.receipt.api.testsupport.installTestJwt

/**
 * LoginRoute 라우팅 테스트
 * - 인증코드 요청/확인
 * - JWT 갱신 (간단한 jwt 인증 구성)
 */
/**
 * @author Devonshin
 * @date 2025-09-13
 */
class LoginRouteTest {

  private val loginService: LoginService = mockk(relaxed = true)

  @Test
  fun `POST login-verification-code-request should return 200`() = testApplication {
    application {
      configureSerialization()
      install(OpenApi) { }
      // 테스트용 JWT 인증 설정
      installTestJwt(authName = "auth-jwt", secret = "dummy-secret")
      routing { loginRoutes(loginService) }
    }

    coEvery { loginService.generateVerificationCode(any()) } returns VerificationCode(
      loginUuid = "uuid-1", expireDate = "2025-12-31 23:59:59"
    )

    val response = client.post("/login/verification-code-request") {
      contentType(ContentType.Application.Json)
      setBody("""{"phone":"01000000000"}""")
    }

    assertEquals(HttpStatusCode.OK, response.status)
  }

  @Test
  fun `POST login-verification-code-check should return 200`() = testApplication {
    application {
      configureSerialization()
      install(OpenApi) { }
      installTestJwt(authName = "auth-jwt", secret = "dummy-secret")
      routing { loginRoutes(loginService) }
    }

    coEvery { loginService.checkVerificationCode(any()) } returns Jwt(
      jwt = "token", expireDate = "2025-12-31 23:59:59", username = "관리자", role = io.allink.receipt.api.domain.admin.MasterRole()
    )

    val response = client.post("/login/verification-code-check") {
      contentType(ContentType.Application.Json)
      setBody("""{"loginUuid":"uuid-1","verificationCode":"123456"}""")
    }

    assertEquals(HttpStatusCode.OK, response.status)
  }

  @Test
  fun `GET jwt-renewal-request should return 200 with jwt auth`() = testApplication {
    val secret = "test-secret"
    val issuer = "test-iss"
    val audience = "test-aud"

    application {
      configureSerialization()
      install(OpenApi) { }
      installTestJwt(authName = "auth-jwt", secret = secret, issuer = issuer, audience = audience)
      routing { loginRoutes(loginService) }
    }

    coEvery { loginService.renewalJwt(any()) } returns Jwt(
      jwt = "token-renewed", expireDate = "2025-12-31 23:59:59", username = "관리자", role = io.allink.receipt.api.domain.admin.MasterRole()
    )

    val token = TestAuth.makeToken(
      secret = secret,
      issuer = issuer,
      audience = audience,
      claims = mapOf(
        "username" to "관리자",
        "lUuid" to "l-uuid",
        "uUuid" to "u-uuid",
        "role" to "MASTER",
        "agencyId" to ""
      )
    )

    val response: HttpResponse = client.get("/login/jwt-renewal-request") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }

    assertEquals(HttpStatusCode.OK, response.status)
  }
}