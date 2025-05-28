package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

@Serializable
@Schema(name = "MerchantGroup", title = "머천트(브랜드) 그룹", description = "실물 태그의 그룹 등록 정보")
data class MerchantGroupModel(
  @Schema(title = "인증키", description = "외부 접속 시 사용할 키", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val tokenKey: String,
  @Schema(title = "소스 아이피", description = "외부 접속 시 허용할 소스 아이피", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val remoteIps: String? = null,
  @Schema(
    title = "서비스 시작일시",
    description = "서비스 시작일시",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED,
    example = "2025-04-17 12:00:00.213123"
  )
  val serviceStartAt: @Contextual LocalDateTime? = null,
  @Schema(
    title = "서비스 종료일시",
    description = "서비스 종료일시",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED,
    example = "2025-04-17 12:00:00.213123"
  )
  val serviceEndAt: @Contextual LocalDateTime? = null,
  @Schema(title = "등록 상태", description = "등록 상태 - 계약 내용에 따라", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val status: MerchantGroupStatus? = null,
  @Schema(
    title = "영수증 너비 인치값",
    description = "포스 프린트 단말기의 영수증 너비 인치값",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  val receiptWidth: String? = null,
  @Schema(
    title = "권한 목록",
    description = "올링크 서비스에서 허용한 권한 목록, 콤마 구분자",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  val authorities: String? = null,
  @Schema(
    title = "영수증 데이터 타입",
    description = "포스 에이전트가 영수증 데이터를 취급하는 방법",
    example = "MERCHANT_RECEIPT|MERCHANT_PARSING|AGENT_HOOKING|AGENT_FILE"
  )
  val receiptType: String? = null,
  @Schema(title = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "머천트 그룹 이름", description = "머천트 그룹의 이름", example = "EDIYA")
  val merchantGroupName: String? = null,
  @Schema(title = "머천트 그룹의 고유 아이디", description = "머천트를 구분할 수 있는 고유한 값")
  override var id: String?,
) : BaseModel<String>

enum class MerchantGroupStatus {
  ACTIVE,
  INACTIVE
}

object MerchantGroupTable : Table("merchant_group") {
  val id = varchar("merchant_group_id", length = 36)
  val tokenKey = varchar("token_key", length = 255)
  val remoteIps = text("remote_ips")
  val serviceStartAt = datetime("service_start_at")
  val serviceEndAt = datetime("service_end_at")
  val status = varchar("status", length = 10)
  val receiptWidth = integer("receipt_width")
  val authorities = varchar("authorities", length = 255)
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date")
  val receiptType = varchar("receipt_type", length = 20)
  val merchantGroupName = varchar("merchant_group_name", length = 50)
  override val primaryKey = PrimaryKey(id)
}