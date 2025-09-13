package io.allink.receipt.api.domain.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.allink.receipt.api.domain.admin.AdminModel
import io.allink.receipt.api.domain.admin.toRole
import io.allink.receipt.api.domain.admin.toRoleString
import io.allink.receipt.api.util.DateUtil
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*

/**
 * @file JwtGenerator.kt
 * @brief JWT 생성 헬퍼 인터페이스 및 기본 구현체
 * @author Devonshin
 * @date 2025-09-12
 */
interface JwtGenerator {
  fun fromLogin(config: ApplicationConfig, loginInfo: LoginInfoModel, adminModel: AdminModel): Jwt
  fun fromPrincipal(config: ApplicationConfig, principal: JWTPrincipal): Jwt
}

class DefaultJwtGenerator : JwtGenerator {
  override fun fromLogin(config: ApplicationConfig, loginInfo: LoginInfoModel, adminModel: AdminModel): Jwt {
    val expiresIn = config.propertyOrNull("jwt.expiresIn")?.getString()?.toLong() ?: 0L
    val expireAt = DateUtil.nowLocalDateTime().plusSeconds(expiresIn)!!
    val nowLocalDateTimeFormat = DateUtil.nowLocalDateTimeFormat(expireAt)
    val token = JWT.create()
      .withAudience(config.propertyOrNull("jwt.audience")?.getString())
      .withIssuer(config.propertyOrNull("jwt.issuer")?.getString())
      .withClaim("username", adminModel.fullName)
      .withClaim("lUuid", loginInfo.id.toString())
      .withClaim("uUuid", loginInfo.userUuid.toString())
      .withClaim("role", adminModel.role.toRoleString())
      .withClaim("agencyId", adminModel.agencyUuid?.toString() ?: "")
      .withExpiresAt(DateUtil.nowInstant(expireAt))
      .sign(Algorithm.HMAC256(config.propertyOrNull("jwt.secret")?.getString()))

    return Jwt(
      jwt = token,
      expireDate = nowLocalDateTimeFormat,
      username = adminModel.fullName,
      role = adminModel.role
    )
  }

  override fun fromPrincipal(config: ApplicationConfig, principal: JWTPrincipal): Jwt {
    val expiresIn = config.propertyOrNull("jwt.expiresIn")?.getString()?.toLong() ?: 0L
    val expireAt = DateUtil.nowLocalDateTime().plusSeconds(expiresIn)!!
    val nowLocalDateTimeFormat = DateUtil.nowLocalDateTimeFormat(expireAt)
    val payload = principal.payload
    val username = payload.getClaim("username").asString()
    val role = payload.getClaim("role").asString()!!

    val token = JWT.create()
      .withAudience(config.propertyOrNull("jwt.audience")?.getString())
      .withIssuer(config.propertyOrNull("jwt.issuer")?.getString())
      .withClaim("username", username)
      .withClaim("lUuid", payload.getClaim("lUuid").asString())
      .withClaim("uUuid", payload.getClaim("uUuid").asString())
      .withClaim("role", role)
      .withClaim("agencyId", payload.getClaim("agencyId").asString())
      .withExpiresAt(DateUtil.nowInstant(expireAt))
      .sign(Algorithm.HMAC256(config.propertyOrNull("jwt.secret")?.getString()))

    return Jwt(jwt = token, expireDate = nowLocalDateTimeFormat, username = username, role = role.toRole()!!)
  }
}
