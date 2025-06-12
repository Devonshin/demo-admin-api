package io.allink.receipt.api.domain.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.allink.receipt.api.domain.admin.AdminModel
import io.allink.receipt.api.domain.admin.AdminService
import io.allink.receipt.api.domain.admin.toRole
import io.allink.receipt.api.domain.admin.toRoleString
import io.allink.receipt.api.domain.sns.VerificationService
import io.allink.receipt.api.exception.InvalidVerificationCodeException
import io.allink.receipt.api.exception.NotFoundUserException
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.util.DateUtil.Companion.nowInstant
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTimeFormat
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LoginServiceImpl(
  private val loginInfoRepository: LoginInfoRepository,
  private val adminService: AdminService,
  private val verificationService: VerificationService,
  private val config: ApplicationConfig
) : LoginService {

  override suspend fun generateVerificationCode(
    verificationCodeRequest: VerificationCodeRequest
  ): VerificationCode = TransactionUtil.withTransaction {
    val verificationCode = adminService.findByPhoneNo(verificationCodeRequest.phone)?.let {
      val code = getCode()
      val expireAt = getExpirationDate()
      val loginInfo = loginInfoRepository.create(
        LoginInfoModel(
          userUuid = it.id!!,
          verificationCode = code,
          expireDate = expireAt,
          status = LoginStatus.PENDING
        )
      )
      val formattedNow = nowLocalDateTimeFormat(expireAt)
      verificationService.sendVerificationMessage(verificationCodeRequest.phone, code, formattedNow)
      VerificationCode(
        loginInfo.id.toString(),
        expireDate = expireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      )
    }
    verificationCode ?: throw NotFoundUserException("Not found user. [${verificationCodeRequest.phone}]")
  }

  override suspend fun checkVerificationCode(
    checkRequest: VerificationCheckRequest
  ): Jwt = TransactionUtil.withTransaction {
    val loginInfo = loginInfoRepository.find(UUID.fromString(checkRequest.loginUuid))?.let {
      if (it.verificationCode != checkRequest.verificationCode) {
        throw InvalidVerificationCodeException("Invalid verification code. [${checkRequest.verificationCode}]")
      } else if (it.status != LoginStatus.PENDING) {
        throw InvalidVerificationCodeException("Invalid login status. [${it.status}]")
      } else if (it.expireDate.isBefore(nowLocalDateTime())) {
        throw InvalidVerificationCodeException("Expired verification code. [${nowLocalDateTimeFormat(it.expireDate)}]")
      }
      it
    }
    if (loginInfo == null) {
      throw InvalidVerificationCodeException("Not found user. [${checkRequest.loginUuid}]")
    }
    adminService.findByUserUuId(loginInfo.userUuid)?.let { adminModel ->
      val count =
        loginInfoRepository.update(loginInfo.copy(status = LoginStatus.ACTIVE, loginDate = nowLocalDateTime()))
      if (count != 1) {
        throw InvalidVerificationCodeException("Failed to update login info. [${checkRequest.loginUuid}]")
      }
      jwtGenerate(config, loginInfo, adminModel)
    }!!
  }

  override suspend fun renewalJwt(principal: JWTPrincipal): Jwt {
    return jwtGenerate(config, principal)
  }

  companion object {
    fun getCode(): String = (100_000..999_999).random().toString()
    fun getExpirationDate(): LocalDateTime = nowLocalDateTime().plusMinutes(5)
    fun jwtGenerate(config: ApplicationConfig, loginInfo: LoginInfoModel, adminModel: AdminModel): Jwt {
      val expireAt =
        nowLocalDateTime().plusSeconds(config.propertyOrNull("jwt.expiresIn")?.getString()?.toLong() ?: 0L)!!
      val nowLocalDateTimeFormat = nowLocalDateTimeFormat(expireAt)
      val token = JWT.create()
        .withAudience(config.propertyOrNull("jwt.audience")?.getString())
        .withIssuer(config.propertyOrNull("jwt.issuer")?.getString())
        .withClaim("username", adminModel.fullName)
        .withClaim("lUuid", loginInfo.id.toString())
        .withClaim("uUuid", loginInfo.userUuid.toString())
        .withClaim("role", adminModel.role.toRoleString())
        .withClaim("agencyId", adminModel.agencyUuid?.toString() ?: "")
        .withExpiresAt(nowInstant(expireAt))
        .sign(Algorithm.HMAC256(config.propertyOrNull("jwt.secret")?.getString()))

      return Jwt(
        jwt = token,
        expireDate = nowLocalDateTimeFormat,
        username = adminModel.fullName,
        role = adminModel.role
      )
    }

    fun jwtGenerate(config: ApplicationConfig, principal: JWTPrincipal): Jwt {
      val expireAt =
        nowLocalDateTime().plusSeconds(config.propertyOrNull("jwt.expiresIn")?.getString()?.toLong() ?: 0L)!!
      val nowLocalDateTimeFormat = nowLocalDateTimeFormat(expireAt)
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
        .withExpiresAt(nowInstant(expireAt))
        .sign(Algorithm.HMAC256(config.propertyOrNull("jwt.secret")?.getString()))

      return Jwt(jwt = token, expireDate = nowLocalDateTimeFormat, username = username, role = role.toRole()!!)
    }
  }
}
