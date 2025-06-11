package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
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
@Schema(name = "merchantInfoModel", title = "머천트 계약자 정보", description = "머천트 계약자의 등록 정보")
data class MerchantInfoModel(
  @Schema(description = "계약자 고유아이디", requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,

  @Schema(description = "영업 대리점 고유아이디", requiredMode = RequiredMode.REQUIRED)
  @Contextual
  val bzAgencyUuid: UUID,

  @Schema(description = "사업자번호")
  val businessNo: String? = null,

  @Schema(description = "대표자명", requiredMode = RequiredMode.REQUIRED)
  val ceoName: String,

  @Schema(description = "CI", requiredMode = RequiredMode.REQUIRED)
  val ceoCi: String,

  @Schema(description = "주민번호")
  val ceoCitizenNo: String,

  @Schema(description = "전화번호")
  val phone: String? = null,

  @Schema(description = "통신사코드")
  val phoneProvider: String,

  @Schema(description = "생년월일")
  val birthday: String,

  @Schema(description = "성별")
  val gender: String? = null,

  @Schema(description = "추천인아이디")
  val referenceUuid: String? = null,

  @Schema(description = "법인번호")
  val corporateNo: String,

  @Schema(description = "등록일시", requiredMode = RequiredMode.REQUIRED)
  val regDate: @Contextual LocalDateTime,

  @Schema(description = "수정일시")
  val modDate: @Contextual LocalDateTime? = null,

  @Schema(description = "등록인 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val regBy: @Contextual UUID,

  @Schema(description = "수정인 고유아이디")
  @Contextual
  val modBy: @Contextual UUID? = null

) : BaseModel<String>


object MerchantInfoTable : Table("merchant_info") {
  val id = uuid("uuid")
  val bzAgencyUuid = uuid("bz_agency_uuid")
  val businessNo = varchar("business_no", 50).nullable()
  val ceoName = varchar("ceo_name", 50)
  val ceoCi = varchar("ceo_ci", 255)
  val ceoCitizenNo = varchar("ceo_citizen_no", 50)
  val phone = varchar("phone", 50).nullable()
  val phoneProvider = varchar("phone_provider", 10)
  val birthday = varchar("birthday", 50).nullable()
  val gender = varchar("gender", 10).nullable()
  val referenceUuid = varchar("reference_uuid", 36).nullable()
  val corporateNo = varchar("corperate_no", 50)
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
  val regBy = uuid("reg_by")
  val modBy = uuid("mod_by").nullable()

  override val primaryKey = PrimaryKey(id)
}
