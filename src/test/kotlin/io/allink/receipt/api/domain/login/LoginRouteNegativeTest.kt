package io.allink.receipt.api.domain.login

import io.allink.receipt.api.config.plugin.configureSerialization
import io.allink.receipt.api.config.plugin.configureStatusPage
import io.allink.receipt.api.config.plugin.configureValidation
import io.allink.receipt.api.testsupport.TestAuth
import io.allink.receipt.api.testsupport.installTestJwt
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * LoginRoute 부정/엣지 테스트
 */
/**
 * @author Devonshin
 * @date 2025-09-13
 */
class LoginRouteNegativeTest {

  private val loginService: LoginService = io.mockk.mockk(relaxed = true)

  @Test
  fun `GET jwt-renewal-request without Authorization should return 401`() = testApplication {
    application {
      configureSerialization()
      configureStatusPage()
      configureValidation()
      installTestJwt()
      routing { loginRoutes(loginService) }
    }
    val resp = client.get("/login/jwt-renewal-request")
    assertEquals(HttpStatusCode.Unauthorized, resp.status)
  }

  @Test
  fun `GET jwt-renewal-request with malformed token should return 401`() = testApplication {
    application {
      configureSerialization()
      configureStatusPage()
      configureValidation()
      installTestJwt()
      routing { loginRoutes(loginService) }
    }
    val resp = client.get("/login/jwt-renewal-request") {
      header(HttpHeaders.Authorization, "Bearer not-a-jwt")
    }
    assertEquals(HttpStatusCode.Unauthorized, resp.status)
  }

  @Test
  fun `GET jwt-renewal-request with expired token should return 401`() = testApplication {
    application {
      configureSerialization()
      configureStatusPage()
      configureValidation()
      installTestJwt()
      routing { loginRoutes(loginService) }
    }
    val expired = TestAuth.makeToken(expiresAtEpochSeconds = 1) // epoch near zero
    val resp = client.get("/login/jwt-renewal-request") {
      header(HttpHeaders.Authorization, "Bearer $expired")
    }
    // Ktor JWT plugin treats expired as Unauthorized
    assertEquals(HttpStatusCode.Unauthorized, resp.status)
  }

  @Test
  fun `POST verification-code-request with invalid body should return 400`() = testApplication {
    application {
      configureSerialization()
      configureStatusPage()
      configureValidation()
      installTestJwt()
      routing { loginRoutes(loginService) }
    }
    val resp = client.post("/login/verification-code-request") {
      contentType(ContentType.Application.Json)
      setBody("{}") // phone 누락
    }
    // 검증 플러그인 없는 경우 라우트 내부 검증 결과에 따름. 스펙상 400으로 매핑 기대.
    // 확실하지 않음: 실제 구현에 따라 400/422 등 달라질 수 있음.
    // 기본 기대값을 400으로 둠.
    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }

  @Test
  fun `POST verification-code-check with empty code should return 400`() = testApplication {
    application {
      configureSerialization()
      configureStatusPage()
      configureValidation()
      installTestJwt()
      routing { loginRoutes(loginService) }
    }
    val resp = client.post("/login/verification-code-check") {
      contentType(ContentType.Application.Json)
      setBody("""{"loginUuid":"uuid-1","verificationCode":""}""") // empty triggers validation
    }
    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }
}
