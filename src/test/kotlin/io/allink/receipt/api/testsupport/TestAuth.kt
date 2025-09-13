package io.allink.receipt.api.testsupport

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.time.Instant
import java.util.Date

/**
 * 테스트용 JWT 인증 헬퍼
 * - 간단한 HMAC256 서명 검증기와 토큰 발급 유틸 제공
 */
/**
 * @author Devonshin
 * @date 2025-09-13
 */
fun Application.installTestJwt(
  authName: String = "auth-jwt",
  secret: String = "test-secret",
  issuer: String? = null,
  audience: String? = null
) {
  install(Authentication) {
    jwt(authName) {
      val req = JWT.require(Algorithm.HMAC256(secret)).apply {
        if (issuer != null) withIssuer(issuer)
        if (audience != null) withAudience(audience)
      }.build()
      verifier(req)
      validate { JWTPrincipal(it.payload) }
    }
  }
}

object TestAuth {
  fun makeToken(
    secret: String = "test-secret",
    issuer: String? = null,
    audience: String? = null,
    claims: Map<String, String> = emptyMap(),
    expiresAtEpochSeconds: Long? = null
  ): String {
    val builder = JWT.create()
    if (issuer != null) builder.withIssuer(issuer)
    if (audience != null) builder.withAudience(audience)
    claims.forEach { (k, v) -> builder.withClaim(k, v) }
    if (expiresAtEpochSeconds != null) builder.withExpiresAt(Date.from(Instant.ofEpochSecond(expiresAtEpochSeconds)))
    return builder.sign(Algorithm.HMAC256(secret))
  }
}
