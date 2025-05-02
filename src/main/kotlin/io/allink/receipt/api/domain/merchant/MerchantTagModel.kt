package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.store.StoreStatus
import io.allink.receipt.api.domain.store.StoreTable
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
  @Schema(title = "가맹점", description = "태그가 등록된 가맹점 ", requiredMode = RequiredMode.NOT_REQUIRED)
  val store: SimpleMerchantStoreDetailModel? = null,
  @Schema(title = "머천트 그룹 아이디", description = "머천트 그룹 고유 아이디", example = "KOCES,EDIYA")
  val merchantGroupId: String?,
  @Schema(title = "태그명", description = "태그명", example = "에이전트 자동 등록태그")
  val tagName: String?,
  @Schema(title = "단말기 아이디", description = "포스, 결제 단말기 고유 아이디", example = "1234567890")
  val deviceId: String?,
  @Schema(title = "가맹점 아이디", description = "올링크에서 관리하는 가맹점 고유아이디", example = "3a931370-cd0b-4427-bf38-418111969c22")
  val storeUid: String?,
  @Schema(title = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", example = "2025-04-17 12:00:00.213123", requiredMode = RequiredMode.NOT_REQUIRED)
  val modDate: @Contextual LocalDateTime?,
) : BaseModel<String>

@Serializable
@Schema(name = "SimpleMerchantTag", title = "머천트 태그", description = "실물 태그의 등록 정보")
data class SimpleMerchantTagModel(
  @Schema(
    title = "태그아이디",
    description = "태그의 고유아이디",
    example = "E001234567890",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String?,
  @Schema(title = "가맹점", description = "태그가 등록된 가맹점 ", requiredMode = RequiredMode.NOT_REQUIRED)
  val store: SimpleMerchantTagStoreModel? = null,
  @Schema(title = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime?,
) : BaseModel<String>

object MerchantTagTable : Table("merchant_tag") {
  val id = varchar("tag_id", 36)
  val merchantStoreId = reference("merchant_store_id", StoreTable.id).nullable()
  val merchantGroupId = reference("merchant_group_id", MerchantGroupTable.id).nullable()
  val tagName = varchar("tag_name", 50).nullable()
  val deviceId = varchar("device_id", 50).nullable()
  val storeUid = varchar("store_uid", 36).nullable()
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
  override val primaryKey = PrimaryKey(id)
}

@Serializable
@Schema(name = "MerchantTagFilter", title = "태그 검색 필터", description = "태그 검색 필터")
data class MerchantTagFilter(
  @Schema(title = "태그 아이디", description = "태그 고유아이디, EQ 검색",  requiredMode = RequiredMode.NOT_REQUIRED)
  val id: String? = null,
  @Schema(title = "가맹점 아이디", description = "가맹점 고유아이디, EQ 검색",  requiredMode = RequiredMode.NOT_REQUIRED)
  val storeId: String? = null,
  @Schema(title = "사업자 번호", description="사업자 번호, EQ 검색", example="1234567890", requiredMode = RequiredMode.NOT_REQUIRED)
  val businessNo: String? = null,
  @Schema(title = "가맹점명", description = "Start with 검색", requiredMode = RequiredMode.NOT_REQUIRED)
  val storeName: String? = null,
  @Schema(title = "프랜차이즈 코드", description = "서비스코드 조회에서 가져온 프랜차이즈 코드, EQ 검색", example = "EDIYA", requiredMode = RequiredMode.NOT_REQUIRED)
  val franchiseCode: String? = null,
  @Schema(title = "검색 기간", description = "검색할 기간의 시작과 끝의 범위", example = """{"from: "2025-04-17T12:00:00", "to: "2025-05-17T12:00:00"}""")
  val period: PeriodFilter,
  @Schema(title = "정렬", requiredMode = RequiredMode.NOT_REQUIRED, exampleClasses = [Sorter::class], description = """
    정렬 필드 : id, name, franchiseCode, regDate, modDate, storeName, storeStatus, businessNo, 
  """)
  val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  val page: Page = Page(1, 10)
)

@Serializable
@Schema(name = "SimpleMerchantTagStoreModel", title = "가맹점", description = "가맹점 등록 정보")
data class SimpleMerchantTagStoreModel(
  @Schema(title = "고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String?,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
  val franchiseCode: String?,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String?,
  @Schema(title = "가맹점 상태", description = "가맹점 상태", example = "ACTIVE", requiredMode = RequiredMode.REQUIRED, allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"])
  val status : StoreStatus?,
) : BaseModel<String>

@Serializable
@Schema(name = "SimpleMerchantStoreDetailModel", title = "가맹점", description = "가맹점 등록 정보")
data class SimpleMerchantStoreDetailModel(
  @Schema(title = "고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String?,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
  val franchiseCode: String?,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String?,
  @Schema(title = "대표자명", description = "대표자명")
  val ceoName: String?,
  @Schema(title = "업종", description = "업종")
  val businessType: String?,
  @Schema(title = "업태", description = "업태")
  val eventType: String?,
  @Schema(title = "가맹점 상태", description = "가맹점 상태", example = "ACTIVE", requiredMode = RequiredMode.REQUIRED, allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"])
  val status : StoreStatus?,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime?,
  @Schema(title = "수정일시", description = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime?,
  @Schema(title = "삭제일시", description = "삭제일시", example = "2025-04-17 12:00:00.213123")
  val deleteDate: @Contextual LocalDateTime?
) : BaseModel<String>
