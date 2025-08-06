package io.allink.receipt.api.domain.user.review

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.review
 * Created: Devonshin
 * Date: 21/04/2025
 */

@Serializable
@Schema(name = "userPointReviewModel", title = "사용자 포인트 리뷰", description = "사용자의 포인트 리뷰 객체")
data class UserPointReviewModel(
  @Schema(
    title = "사용자 포인트 리뷰 고유아이디",
    description = "사용자의 포인트 리뷰 고유아이디",
    requiredMode = RequiredMode.REQUIRED,
    example = "3a931370-cd0b-4427-bf38-418111969c22"
  )
  override var id: String?,
  @Schema(title = "사용자 고유 아이디", description = "사용자 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val userUuid: String,
  @Schema(title = "포인트 가맹점 고유아이디", description = "포인트 가맹점 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val storeUid: String,
  @Schema(title = "영수증 고유 아이디", description = "영수증 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val receiptUuid: String,
  @Schema(title = "현재 상태", description = "리뷰 작성 상태", requiredMode = RequiredMode.REQUIRED)
  val status: UserReviewStatus,
  @Schema(title = "리뷰 포인트", description = "리뷰 지급 포인트")
  val points: Int?,
  @Schema(title = "리뷰 작성 만료일시", description = "리뷰 URL 입력 만료일시", example = "2025-03-05T13:08:12.152764")
  val expireDate: @Contextual LocalDateTime?,
  @Schema(title = "리뷰 URL", description = "사용자 입력 리뷰 URL")
  val reviewUrl: String?,
  @Schema(title = "등록일시", description = "리뷰 등록 신청 일시", example = "2025-03-05T13:08:12.152764")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", description = "리뷰 등록 수정 일시", example = "2025-03-05T13:08:12.152764")
  val modDate: @Contextual LocalDateTime?,
) : BaseModel<String>

@Schema(name = "UserReviewStatus", title = "사용자 리뷰", description = "사용자 리뷰 진행 상태", enumAsRef = true)
enum class UserReviewStatus(val desc: String) {
  WRITING("작성 중"),
  APPLIED("URL 입력완료"),
  APPROVED("리뷰 확인완료"),
  COMPLETED("완료"),
  REJECTED("반려");
}

object UserPointReviewTable : Table("n_point_user_review") {
  val id = varchar("receipt_uuid", length = 36)
  val userUuid = varchar("user_uuid", length = 36)
  val storeUid = varchar("store_uid", length = 36)
  val status = enumerationByName<UserReviewStatus>("status", length = 10)
  val reviewUrl = varchar("review_url", length = 255).nullable()
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
  val points = integer("points").nullable()
  val expireDate = datetime("expire_date").nullable()
  override val primaryKey = PrimaryKey(id)
}
