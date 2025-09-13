package io.allink.receipt.api.domain.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.allink.receipt.api.domain.admin.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * JwtGenerator 단위 테스트
 * - fromLogin / fromPrincipal 경로 커버리지 확보
 */
/**
 * @author Devonshin
 * @date 2025-09-13
 */
class JwtGeneratorTest {

  private val config: ApplicationConfig = MapApplicationConfig(
    "jwt.expiresIn" to "3600",
    "jwt.audience" to "test-aud",
    "jwt.issuer" to "test-iss",
    "jwt.secret" to "test-secret"
  )

  private val generator: JwtGenerator = DefaultJwtGenerator()

  @Test
  fun `fromLogin should build token with expected claims`() {
    // given
    val admin = AdminModel(
      id = UUID.randomUUID(),
      fullName = "관리자",
      role = MasterRole(),
      phone = "01000000000",
      status = AdminStatus.ACTIVE
    )
    val loginInfo = LoginInfoModel(
      id = UUID.randomUUID(),
      userUuid = admin.id!!,
      verificationCode = "123456",
      expireDate = LocalDateTime.now().plusMinutes(5),
      status = LoginStatus.PENDING
    )

    // when
    val jwt = generator.fromLogin(config, loginInfo, admin)

    // then
    assertNotNull(jwt.jwt)
    val decoded = JWT.decode(jwt.jwt)
    assertEquals("관리자", decoded.getClaim("username").asString())
    assertEquals(admin.id!!.toString(), decoded.getClaim("uUuid").asString())
    assertEquals("MASTER", decoded.getClaim("role").asString())
  }

  @Test
  fun `fromPrincipal should renew token with same payload values`() {
    // given: 기존 토큰을 만들어 principal 준비
    val lUuid = UUID.randomUUID().toString()
    val uUuid = UUID.randomUUID().toString()
    val baseToken = JWT.create()
      .withAudience(config.property("jwt.audience").getString())
      .withIssuer(config.property("jwt.issuer").getString())
      .withClaim("username", "관리자")
      .withClaim("lUuid", lUuid)
      .withClaim("uUuid", uUuid)
      .withClaim("role", "MASTER")
      .withClaim("agencyId", "")
      .sign(Algorithm.HMAC256(config.property("jwt.secret").getString()))
    val principal = JWTPrincipal(JWT.decode(baseToken))

    // when
    val jwt = generator.fromPrincipal(config, principal)

    // then
    assertNotNull(jwt.jwt)
    val decoded = JWT.decode(jwt.jwt)
    assertEquals("관리자", decoded.getClaim("username").asString())
    assertEquals(lUuid, decoded.getClaim("lUuid").asString())
    assertEquals(uUuid, decoded.getClaim("uUuid").asString())
    assertEquals("MASTER", decoded.getClaim("role").asString())
  }
}