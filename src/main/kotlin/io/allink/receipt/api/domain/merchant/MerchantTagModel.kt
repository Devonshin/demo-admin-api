package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.*
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
@Schema(name = "merchantTag", title = "머천트 태그", description = "실물 태그의 등록 정보")
data class MerchantTagModel(
  @Schema(
    title = "태그아이디",
    description = "태그의 고유아이디",
    example = "E001234567890",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String? = null,
  @Schema(title = "가맹점", description = "태그가 등록된 가맹점 ")
  val store: SimpleMerchantStoreDetailModel? = null,
  @Schema(title = "머천트 그룹 아이디", description = "머천트 그룹 고유 아이디", example = "KOCES,EDIYA")
  val merchantGroupId: String? = null,
  @Schema(title = "머천트 가맹점 아이디", description = "머천트 가맹점 고유 아이디")
  val merchantStoreId: String? = null,
  @Schema(title = "태그명", description = "태그명", example = "에이전트 자동 등록태그")
  val tagName: String? = null,
  @Schema(title = "단말기 아이디", description = "포스, 결제 단말기 고유 아이디", example = "1234567890")
  val deviceId: String? = null,
  @Schema(title = "가맹점 아이디", description = "올링크에서 관리하는 가맹점 고유아이디", example = "3a931370-cd0b-4427-bf38-418111969c22")
  val storeUid: String? = null,
  @Schema(title = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
  val regBy: @Contextual UUID? = null,
  val modBy: @Contextual UUID? = null,
) : BaseModel<String>

@Serializable
@Schema(name = "simpleMerchantTag", title = "머천트 태그", description = "실물 태그의 등록 정보")
data class SimpleMerchantTagModel(
  @Schema(
    title = "태그아이디",
    description = "태그의 고유아이디",
    example = "E001234567890",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String? = null,
  @Schema(title = "태그명", description = "태그명 ")
  val name: String? = null,
  @Schema(title = "가맹점", description = "태그가 등록된 가맹점 ")
  val store: SimpleMerchantTagStoreModel? = null,
  @Schema(title = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
) : BaseModel<String>

object MerchantTagTable : Table("merchant_tag") {
  val id = varchar("tag_id", 36)
  val storeUid = varchar("store_uid", 36).nullable()
  val merchantGroupId = reference("merchant_group_id", MerchantGroupTable.id).nullable()
  val merchantStoreId = varchar("merchant_store_id", 36).nullable()
  val tagName = varchar("tag_name", 50).nullable()
  val deviceId = varchar("device_id", 50).nullable()
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
  val regBy = uuid(name = "reg_by")
  val modBy = uuid(name = "mod_by").nullable()
  override val primaryKey = PrimaryKey(id)
}

@Serializable
@Schema(name = "MerchantTagFilter", title = "태그 검색 필터", description = "태그 검색 필터")
data class MerchantTagFilter(
  @Schema(title = "태그 아이디", description = "태그 고유아이디, EQ 검색")
  val id: String? = null,
  @Schema(title = "태그명", description = "태그명, Start with 검색")
  val name: String? = null,
  @Schema(title = "가맹점 아이디", description = "가맹점 고유아이디, EQ 검색")
  val storeId: String? = null,
  @Schema(title = "사업자 번호", description = "사업자 번호, EQ 검색", example = "123-45-67890")
  val businessNo: String? = null,
  @Schema(title = "가맹점명", description = "Start with 검색")
  val storeName: String? = null,
  @Schema(title = "프랜차이즈 코드", description = "서비스코드 조회에서 가져온 프랜차이즈 코드, EQ 검색", example = "EDIYA")
  val franchiseCode: String? = null,
  @Schema(
    title = "검색 기간",
    description = "검색할 기간의 시작과 끝의 범위",
    example = """{"from: "2025-04-17T12:00:00", "to: "2025-05-17T12:00:00"}"""
  )
  val period: PeriodFilter,
  @Schema(
    title = "정렬", exampleClasses = [Sorter::class], description = """
    정렬 필드 : id, name, franchiseCode, regDate, modDate, storeName, businessNo 
  """
  )
  override val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
) : BaseFilter

@Serializable
@Schema(name = "simpleMerchantTagStoreModel", title = "태그의 가맹점 요약", description = "태그 목록의 가맹점 등록 요약 정보")
data class SimpleMerchantTagStoreModel(
  @Schema(title = "고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String? = null,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
  val franchiseCode: String? = null,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String? = null,
  @Schema(
    title = "가맹점 상태",
    description = "가맹점 상태",
    example = "ACTIVE",
    requiredMode = RequiredMode.REQUIRED,
    allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"]
  )
  val status: StatusCode? = null,
) : BaseModel<String>


@Serializable
@Schema(name = "merchantTagModifyModel", title = "가맹점", description = "가맹점 등록 정보")
data class MerchantTagModifyModel(
  @Schema(title = "태그 아이디", description = "태그 아이디", requiredMode = RequiredMode.REQUIRED)
  val id: String,
  @Schema(title = "태그명", description = "태그명")
  val name: String? = null,
  @Schema(title = "가맹점 고유아이디", description = "가맹점 고유아이디")
  val storeId: String? = null,
  @Schema(title = "디바이스 아이디", description = "태그와 연결된 단말기 고유아이디")
  val deviceId: String? = null,
)

@Serializable
@Schema(name = "simpleMerchantStoreDetailModel", title = "가맹점", description = "가맹점 등록 정보")
data class SimpleMerchantStoreDetailModel(
  @Schema(title = "고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String? = null,
  @Schema(title = "프랜차이즈 코드", description = "프랜차이즈 코드")
  val franchiseCode: String? = null,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String? = null,
  @Schema(title = "대표자명", description = "대표자명")
  val ceoName: String? = null,
  @Schema(title = "대표 전화번호", description = "대표 전화번호")
  val tel: String? = null,
  @Schema(title = "업종", description = "업종")
  val businessType: String? = null,
  @Schema(title = "업태", description = "업태")
  val eventType: String? = null,
  @Schema(title = "단말기 구분", description = "단말기 구분", example = "CAT, OKPOS,...")
  val deviceType: String? = null,
  @Schema(
    title = "가맹점 상태",
    description = "가맹점 상태",
    example = "ACTIVE",
    requiredMode = RequiredMode.REQUIRED,
    allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"]
  )
  val status: StatusCode? = null,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "수정일시", description = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "삭제일시", description = "삭제일시", example = "2025-04-17 12:00:00.213123")
  val deleteDate: @Contextual LocalDateTime?
) : BaseModel<String>
