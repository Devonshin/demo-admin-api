package io.allink.receipt.api.domain.code

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

/**
 * Package: io.allink.receipt.api.domain.code
 * Created: Devonshin
 * Date: 17/04/2025
 */

@Serializable
@Schema(name = "serviceCode", title = "서비스 코드", description = "각종 코드입니다")
data class ServiceCodeModel(
  @Schema(title = "코드값", description = "코드값", requiredMode = RequiredMode.REQUIRED, example = "EDIYA")
  override var id: String?,
  @Schema(title = "코드 그룹", description = "코드 그룹", requiredMode = RequiredMode.REQUIRED, example = "FRANCHISE | MERT_SVC")
  val serviceGroup: String,
  @Schema(title = "코드명", description = "코드명", requiredMode = RequiredMode.REQUIRED, example = "EDIYA")
  val serviceName: String,
  @Schema(title = "금액", description = "금액", requiredMode = RequiredMode.NOT_REQUIRED)
  val price: Int?,
  @Schema(title = "상태", description = "상태", allowableValues = ["ACTIVE", "INACTIVE"],requiredMode = RequiredMode.NOT_REQUIRED)
  val status: ServiceCodeStatus?,
): BaseModel<String>

object ServiceCodeTable: Table("service_code") {
  val serviceCode = varchar("service_code", 30)
  val serviceGroup = varchar("service_group", 10)
  val serviceName = varchar("service_name", 255)
  val price = integer("price").nullable()
  val status = enumerationByName<ServiceCodeStatus>("status", 10).nullable()
  override val primaryKey = PrimaryKey(serviceCode)
}

enum class ServiceCodeStatus {
  ACTIVE,
  INACTIVE
}

enum class ServiceCodeGroup {
  FRANCHISE,
  BANK_CODE,
  VEN_CODE,
  MERT_SVC
}