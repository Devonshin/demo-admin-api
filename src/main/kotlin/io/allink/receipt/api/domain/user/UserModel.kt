package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.BaseModel
import io.allink.receipt.api.common.Page
import io.allink.receipt.api.common.Sorter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */
@Serializable
@Schema(title = "사용자 객체", description = "모영 회원 가입자")
data class UserModel(
  @Schema(title = "고유아이디", description = "사용자 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "이름", description = "사용자 이름", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val name: String?,
  @Schema(title = "회원가입 상태", description = "회원 가입 상태값", nullable = false, requiredMode = RequiredMode.REQUIRED, exampleClasses = [UserStatus::class])
  val status: UserStatus?,
  @Schema(title = "휴대폰번호", description = "휴대폰번호", nullable = false, requiredMode = RequiredMode.REQUIRED, example = "01012349876")
  val phone: String?,
  @Schema(title = "성별", description = "성별", nullable = false, requiredMode = RequiredMode.REQUIRED, example = "F|M")
  val gender: String?,
  @Schema(title = "CI 값", description = "CI", nullable = false, requiredMode = RequiredMode.REQUIRED, hidden = true)
  @Transient
  val ci: String?,
  @Schema(title = "생년월일", description = "생년월일", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val birthday: String?,
  @Schema(title = "내국인여부", description = "내국인여부값", example = "Y|N")
  val localYn: String?,
  @Schema(title = "이메일", description = "이메일 주소", nullable = true)
  val email: String?,
  @Schema(title = "권한", description = "사용자 권한", exampleClasses = [UserRole::class], example = "USER|TEMP")
  val role: UserRole?,
  @Schema(title = "회원 가입 경로", description = "회원가입 경로", example = "NAVER|KAKAO")
  val joinSocialType: String?,
  @Schema(title = "닉네임", description = "소셜 연동 닉네임")
  val nickname: String?,
  @Schema(title = "탄소포인트 회원 연동아이디", description = "탄소포인트 회원 연동아이디")
  val mtchgId: String?,
  @Schema(title = "전자문서 발송기관", description = "전자문서 발송 기관", example = "kakao|naver")
  val cpointRegType: String?,
  @Schema(title = "탄소포인트 회원 가입일시", description = "탄소포인트 회원 가입일시")
  val cpointRegDate: @Contextual LocalDateTime?,
  @Schema(title = "회원 가입일시", description = "회원 가입일시", example = "2025-03-05T13:08:12.152764")
  val regDate: @Contextual LocalDateTime?,
  @Schema(title = "수정일시", description = "회원 수정일시", example = "2025-03-05T13:08:12.152764")
  val modDate: @Contextual LocalDateTime?
) : BaseModel<String>

@Serializable
@Schema(title = "사용자 객체", description = "모영 회원 가입자 약식 정보")
data class SimpleUserModel(
  @Schema(title = "고유아이디", description = "사용자 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "이름", description = "사용자 이름", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val name: String?,
) : BaseModel<String>

object UserTable : Table("user") {
  val id = varchar("uuid", 36)
  val name = varchar("name", 255).nullable()
  val status = enumerationByName("status", 20, UserStatus::class).nullable()
  val phone = varchar("phone", 50).nullable()
  val gender = varchar("gender", 50).nullable()
  val ci = varchar("ci", 255).nullable()
  val birthday = varchar("birthday", 50).nullable()
  val localYn = varchar("local_yn", 1).nullable()
  val email = varchar("email", 255).nullable()
  val regDate = datetime("reg_date").nullable()
  val modDate = datetime("mod_date").nullable()
  val role = enumerationByName("role", 20, UserRole::class).nullable()
  val joinSocialType = varchar("join_social_type", 20).nullable()
  val nickname = varchar("nickname", 255).nullable()
  val mtchgId = varchar("mtchg_id", 255).nullable()
  val cpointRegType = varchar("cpoint_reg_type", 20).nullable()
  val cpointRegDate = datetime("cpoint_reg_date").nullable()
  override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

@Schema(title = "사용자 가입 상태", description = "사용자 가입 상태")
enum class UserStatus {
  ACTIVE,
  NORMAL,
  INACTIVE;

  companion object {
    fun from(value: String): UserStatus {
      return try {
        valueOf(value.uppercase())
      } catch (e: IllegalArgumentException) {
        throw IllegalStateException("Unknown UserStatus: $value")
      }
    }
  }
}

@Schema(title = "사용자 가입 권한", description = "사용자 가입 권한")
enum class UserRole {
  USER,
  TEMP
}

@Serializable
@Schema(title = "사용자 검색 필터", description = "사용자 검색 필터")
data class UserFilter(
  @Schema(title = "휴대폰번호", description = "Eq 검색만 가능", requiredMode = RequiredMode.NOT_REQUIRED)
  val phone: String? = null,
  @Schema(title = "이름", description = "Start with 검색",requiredMode = RequiredMode.NOT_REQUIRED)
  val name: String? = null,
  @Schema(title = "닉네임", description = "Eq 검색만 가능", requiredMode = RequiredMode.NOT_REQUIRED)
  val nickName: String? = null,
  @Schema(title = "연령대", requiredMode = RequiredMode.NOT_REQUIRED)
  val age: Age? = null,
  @Schema(title = "성별", example = "M|F", requiredMode = RequiredMode.NOT_REQUIRED)
  val gender: String? = null,
  @Schema(title = "정렬", requiredMode = RequiredMode.NOT_REQUIRED, exampleClasses = [Sorter::class])
  val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  val page: Page = Page(1, 10)
)

@Serializable
@Schema(title = "나이 대")
data class Age(
  @Schema(title = "시작 출생년도", description = "종료 출생년보다 빠른 값", example = "2002", requiredMode = RequiredMode.NOT_REQUIRED)
  val from: String,
  @Schema(title = "종료 출생년도", example = "2022", requiredMode = RequiredMode.NOT_REQUIRED)
  val to: String
)
