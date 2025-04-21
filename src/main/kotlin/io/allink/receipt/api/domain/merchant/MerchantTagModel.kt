package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.common.BaseModel
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
@Schema(name = "MerchantTag", title = "머천트 태그", description = "실물 태그의 등록 정보")
data class MerchantTagModel(
  @Schema(
    title = "태그아이디",
    description = "태그의 고유아이디",
    example = "E001234567890",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String?,
  @Schema(title = "머천트 소유 가맹점 아이디", description = "머천트가 관리하는 가맹점 아이디", example = "1234567890")
  val merchantStoreId: String,
  @Schema(title = "머천트 그룹 아이디", description = "머천트 그룹 고유 아이디", example = "KOCES,EDIYA")
  val merchantGroupId: String,
  @Schema(title = "단말기 아이디", description = "포스, 결제 단말기 고유 아이디", example = "1234567890")
  val deviceId: String,
  @Schema(title = "가맹점 아이디", description = "올링크에서 관리하는 가맹점 고유아이디", example = "3a931370-cd0b-4427-bf38-418111969c22")
  val storeUid: String,
  @Schema(title = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime?,
) : BaseModel<String>

@Serializable
@Schema(name = "simpleMerchantTag", title = "머천트 태그", description = "실물 태그의 약식 등록 정보")
data class SimpleMerchantTagModel(
  @Schema(
    title = "태그아이디",
    description = "태그의 고유아이디",
    example = "E001234567890",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String?,
  @Schema(title = "단말기 아이디", description = "포스, 결제 단말기 고유 아이디", example = "1234567890")
  val deviceId: String?,
) : BaseModel<String>

object MerchantTagTable : Table("merchant_tag") {
  val id = varchar("tag_id", 36)
  val merchantStoreId = varchar("merchant_store_id", 36)
  val merchantGroupId = reference("merchant_group_id", MerchantGroupTable.id)
  val deviceId = varchar("device_id", 50)
  val storeUid = varchar("store_uid", 36)
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date")
  override val primaryKey = PrimaryKey(id)
}