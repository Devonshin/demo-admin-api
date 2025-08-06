package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

@Serializable
@Schema(name = "storeBillingTokenModel", title = "가맹점 결제 카드 토큰 정보", description = "가맹점 결제 카드 토큰 등록 정보")
data class StoreBillingTokenModel(
  @Schema(title = "토큰 고유아이디", description = "토큰 고유아이디")
  override var id: @Contextual UUID? = null,
  @Schema(title = "사업자 번호", description = "사업자 번호")
  val businessNo: String,
  @Schema(title = "SK결제토큰", description = "SK결제토큰")
  val token: String,
  @Schema(title = "토큰추가정보", description = "토큰추가정보")
  val tokenInfo: String? = null,
  @Schema(title = "상태", description = "토큰 상태")
  val status: StatusCode,
  @Schema(title = "등록일시", description = "토큰 등록일시")
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "등록인 고유아이디", description = "토큰을 등록한 사람의 고유아이디")
  val regBy: @Contextual UUID
) : BaseModel<UUID>


object StoreBillingTokenTable : Table("store_billing_token") {

  val id = uuid("token_uuid") // 토큰 고유아이디
  val businessNo = varchar("business_no", 12).index()
  val token = varchar("token", 255) // SK결제토큰
  val tokenInfo = varchar("token_info", 255).nullable() // 토큰추가정보
  val status = enumerationByName<StatusCode>("status", length = 20) // 상태
  val regDate = datetime("reg_date").nullable() // 등록일시
  val regBy = uuid("reg_by") // 등록인 고유아이디

  override val primaryKey = PrimaryKey(id, name = "pk_merchant_billing_token") // 기본 키

}
