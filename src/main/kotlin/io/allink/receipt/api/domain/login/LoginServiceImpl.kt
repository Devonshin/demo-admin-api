package io.allink.receipt.api.domain.login

import io.allink.receipt.api.domain.admin.AdminService
import io.allink.receipt.api.domain.sns.VerificationService
import io.allink.receipt.api.exception.InvalidVerificationCodeException
import io.allink.receipt.api.exception.NotFoundUserException
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.util.DateUtil
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LoginServiceImpl(
  private val loginInfoRepository: LoginInfoRepository,
  private val adminService: AdminService,
  private val verificationService: VerificationService,
  private val config: ApplicationConfig,
  private val jwtGenerator: JwtGenerator
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
      val formattedNow = DateUtil.nowLocalDateTimeFormat(expireAt)
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
      validateVerificationCode(it, checkRequest)
    }
    if (loginInfo == null) {
      throw InvalidVerificationCodeException("Not found user. [${checkRequest.loginUuid}]")
    }
    adminService.findByUserUuId(loginInfo.userUuid)?.let { adminModel ->
      val count =
        loginInfoRepository.update(loginInfo.copy(status = LoginStatus.ACTIVE, loginDate = DateUtil.nowLocalDateTime()))
      if (count != 1) {
        throw InvalidVerificationCodeException("Failed to update login info. [${checkRequest.loginUuid}]")
      }
      jwtGenerator.fromLogin(config, loginInfo, adminModel)
    }!!
  }

  private fun validateVerificationCode(
    model: LoginInfoModel,
    checkRequest: VerificationCheckRequest
  ): LoginInfoModel {
    if (model.verificationCode != checkRequest.verificationCode) {
      throw InvalidVerificationCodeException("Invalid verification code. [${checkRequest.verificationCode}]")
    } else if (model.status != LoginStatus.PENDING) {
      throw InvalidVerificationCodeException("Invalid login status. [${model.status}]")
    } else if (model.expireDate.isBefore(DateUtil.nowLocalDateTime())) {
      throw InvalidVerificationCodeException("Expired verification code. [${DateUtil.nowLocalDateTimeFormat(model.expireDate)}]")
    }
    return model
  }

  override suspend fun renewalJwt(principal: JWTPrincipal): Jwt {
    return jwtGenerator.fromPrincipal(config, principal)
  }

  companion object {
    fun getCode(): String = (100_000..999_999).random().toString()
    fun getExpirationDate(): LocalDateTime = DateUtil.nowLocalDateTime().plusMinutes(5)
  }
}
