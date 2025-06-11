package io.allink.receipt.api.domain.merchant

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
@Schema(name = "merchantBillingTokenModel", title = "머천트 계약자 결제 카드 토큰 정보", description = "머천트 계약자의 결제 카드 토큰 등록 정보")
data class MerchantBillingTokenModel(
  @Schema(title = "토큰 고유아이디", description = "토큰 고유아이디")
  override var id: @Contextual UUID? = null,
  @Schema(title = "계약자 고유아이디", description = "계약자 고유아이디")
  val merchantInfoUuid: @Contextual UUID,
  @Schema(title = "SK결제토큰", description = "SK결제토큰")
  val token: String,
  @Schema(title = "토큰추가정보", description = "토큰추가정보")
  val tokenInfo: String? = null,
  @Schema(title = "상태", description = "토큰 상태")
  val status: String,
  @Schema(title = "등록일시", description = "토큰 등록일시")
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "수정일시", description = "토큰 수정일시")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "등록인 고유아이디", description = "토큰을 등록한 사람의 고유아이디")
  val regBy: @Contextual UUID,
  @Schema(title = "수정인 고유아이디", description = "토큰을 수정한 사람의 고유아이디")
  val modBy: @Contextual UUID

) : BaseModel<UUID>


object MerchantBillingTokenTable : Table("merchant_billing_token") {
  val tokenUuid = uuid("token_uuid") // 토큰 고유아이디
  val merchantInfoUuid = reference("merchant_info_uuid", MerchantInfoTable.id) // 계약자 고유아이디
  val token = varchar("token", 255) // SK결제토큰
  val tokenInfo = varchar("tokenInfo", 255).nullable() // 토큰추가정보
  val status = varchar("status", 10) // 상태
  val regDate = datetime("reg_date").nullable() // 등록일시
  val regBy = uuid("reg_by") // 등록인 고유아이디

  override val primaryKey = PrimaryKey(tokenUuid, name = "pk_merchant_billing_token") // 기본 키
  val id = tokenUuid // 다른 테이블에서 참조할 수 있도록 id 별칭 제공

}
