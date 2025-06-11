package io.allink.receipt.api.domain.receipt.edoc

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 20/04/2025
 */

@Serializable
@Schema(name = "naverEdocModel", title = "네이버 전자문서", description = "네이버 전자문서로 발송된 영수증")
data class NaverEdocModel(
  @Schema(title = "사용자 영수증 고유아이디", description = "사용자의 영수증 고유아이디", requiredMode = RequiredMode.REQUIRED, example = "3a931370-cd0b-4427-bf38-418111969c22")
  override var id: String?,  /*partnerReqUuid*/
  @Schema(title = "영수증 고유아이디", description = "영수증 고유아이디", requiredMode = RequiredMode.REQUIRED, example = "1234faa-cd0b-4427-bf38-418111969c22")
  val receiptUuid: String,
  @Schema(title = "전자문서 아이디", description = "네이버에서 발행한 전자문서 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val envelopId: String, /*naverDocId*/
  @Schema(title = "전송 결과 코드", description = "네이버에서 발행한 전송 결과 코드", requiredMode = RequiredMode.REQUIRED)
  val responseCode: String,
  @Schema(title = "발송일시", description = "전자문서 발송 요청일시", requiredMode = RequiredMode.REQUIRED, example = "2025-03-05T13:08:12.152764")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "사용자 고유이이디", description = "영수증 사용자 고유아이디", requiredMode = RequiredMode.REQUIRED, example = "2025-03-05T13:08:12.152764")
  val userId: String,
): BaseModel<String>

object NaverBillTable : Table("naver_bill") {
    val receiptUuid = varchar("receipt_uuid", length = 36)
    val envelopId = varchar("naver_doc_id", length = 100)
    val responseCode = varchar("response_code", length = 30)
    val regDate = datetime("reg_date")
    val partnerReqUuid = varchar("partner_req_uuid", length = 36)
    val userId = varchar("user_id", length = 36)
    override val primaryKey = PrimaryKey(partnerReqUuid)
}
