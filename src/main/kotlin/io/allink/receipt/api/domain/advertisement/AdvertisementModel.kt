package io.allink.receipt.api.domain.advertisement

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

/**
 * Package: io.allink.receipt.api.domain.advertizement
 * Created: Devonshin
 * Date: 21/04/2025
 */

@Serializable
@Schema(name = "AdvertisementModel", title = "광고")
data class AdvertisementModel(
  @Schema(title = "광고주 머천트 고유아이디", description = "광고주 머천트 고유아이디")
  val merchantGroupId: String,
  @Schema(title = "광고주 타이틀", description = "광고 타이틀")
  val title: String,
  @Schema(title = "등록일시", description = "등록 일시", example = "2025-03-05T13:08:12.152764")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", description = "수정 일시", example = "2025-03-05T13:08:12.152764")
  val modDate: @Contextual LocalDateTime?,
  override var id: @Contextual UUID? = null
): BaseModel<UUID>

@Serializable
@Schema(name = "SimpleAdvertisementModel", title = "광고")
data class SimpleAdvertisementModel(
  @Schema(title = "광고주 머천트 고유아이디", description = "광고주 머천트 고유아이디")
  val merchantGroupId: String?,
  @Schema(title = "광고주 타이틀", description = "광고 타이틀")
  val title: String?,
  override var id: @Contextual UUID? = null
): BaseModel<UUID>

object AdvertisementTable: UUIDTable(name = "advertisement", columnName = "uuid") {
  val merchantGroupId = varchar("merchant_group_id", 36)
  val title = varchar("title", 255)
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
}