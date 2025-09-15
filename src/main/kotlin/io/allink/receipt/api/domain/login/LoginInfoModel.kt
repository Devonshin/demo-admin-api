package io.allink.receipt.api.domain.login

import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.admin.AdminTable
import io.allink.receipt.api.domain.admin.Role
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.login
 * Created: Devonshin
 * Date: 14/04/2025
 */

@Serializable
data class LoginInfoModel(
  override var id: @Contextual UUID? = null,
  val userUuid: @Contextual UUID,
  val verificationCode: String,
  val expireDate: @Contextual LocalDateTime,
  val status: LoginStatus,
  val loginDate: @Contextual LocalDateTime? = null,
) : BaseModel<UUID>

object LoginInfoTable : UUIDTable(name = "login_info", columnName = "login_uuid") {
  val userUuid = reference("user_uuid", AdminTable.id)
  val verificationCode = text("verification_code")
  val expireDate = datetime("expire_date")
  val status = enumerationByName("status", 20, LoginStatus::class)
  val loginDate = datetime("login_date").nullable()
}

enum class LoginStatus {
  ACTIVE,
  INACTIVE,
  PENDING,
}

@Serializable @Schema(description = "인증 코드 요청 객체", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class VerificationCode(
  @param:Schema(description = "인증코드 고유 아이디 - 인증코드와 같이 로그인 요청 시 전송")
  val loginUuid: String,
  @param:Schema(description = "인증코드 만료일시")
  val expireDate: String
)

@Serializable @Schema(description = "아이디,패스워드 로그인 객체", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class LoginRequest(
  @param:Schema(description = "아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val username: String,
  @param:Schema(description = "패스워드", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val password: String
)

@Serializable @Schema(description = "인증 코드 확인 객체", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class VerificationCheckRequest(
  @param:Schema(description = "인증코드 고유 아이디 - 인증 코드 요청 시 받은 고유값")
  val loginUuid: String,
  @param:Schema(description = "인증코드")
  val verificationCode: String
)

@Serializable @Schema(description = "인증 코드 요청 객체", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class VerificationCodeRequest(
  @param:Schema(description = "인증 코드를 수신할 휴대폰 번호", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val phone: String
)

@Serializable @Schema(description = "Jwt 객체", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class Jwt(
  @param:Schema(description = "Jwt 데이터", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val jwt: String,
  @param:Schema(description = "만료일시", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val expireDate: String,
  @param:Schema(description = "사용자명", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val username: String,
  @param:Schema(description = "권한", nullable = false, requiredMode = RequiredMode.REQUIRED, example = "")
  @Polymorphic
  val role: Role,
)