package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.BaseFilter
import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.domain.Sorter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */
@Serializable
@Schema(name = "storeModel", title = "가맹점", description = "가맹점 등록 정보")
data class StoreModel(
  @Schema(title = "고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
  val franchiseCode: String? = null,
  @Schema(title = "지역코드", description = "지역코드")
  val zoneCode: String? = null,
  @Schema(title = "도로명, 지번 주소", description = "도로명, 지번 주소")
  val addr1: String? = null,
  @Schema(title = "상세 주소", description = "상세 주소")
  val addr2: String? = null,
  @Schema(title = "지도 url", description = "지도 url")
  val mapUrl: String? = null,
  @Schema(title = "위도 좌표", description = "위도 좌표")
  val lat: String? = null,
  @Schema(title = "경도 좌표", description = "경도 좌표")
  val lon: String? = null,
  @Schema(title = "대표자 전화번호", description = "대표자 전화번호")
  val tel: String? = null,
  @Schema(title = "담당자 번호", description = "담당자 번호")
  val mobile: String? = null,
  @Schema(title = "담당자명", description = "담당자명")
  val managerName: String? = null,
  @Schema(title = "사이트 주소", description = "사이트 주소")
  val siteLink: String? = null,
  @Schema(title = "워크타입", description = "워크타입")
  val workType: String? = null,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String? = null,
  @Schema(title = "법인 등록번호", description = "법인 등록번호")
  val businessNoLaw: String? = null,
  @Schema(title = "대표자명", description = "대표자명")
  val ceoName: String? = null,
  @Schema(title = "업종", description = "업종")
  val businessType: String? = null,
  @Schema(title = "업태", description = "업태")
  val eventType: String? = null,
  @Schema(title = "이메일", description = "이메일")
  val email: String? = null,
  @Schema(title = "가맹점 타입", description = "가맹점 타입")
  val storeType: String? = null,
  @Schema(title = "아이콘 URL", description = "아이콘 URL")
  val iconUrl: String? = null,
  @Schema(title = "로고 URL", description = "로고 URL")
  val logoUrl: String? = null,
  @Schema(title = "영수증 너비 인치", description = "영수증 너비 인치")
  val receiptWidthInch: String? = null,
  @Schema(title = "가맹점 상태", description = "가맹점 상태코드", example = "ACTIVE", requiredMode = RequiredMode.REQUIRED, allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"])
  val status: StoreStatus? = null,
  @Schema(title = "가맹점 로그인 아이디", description = "가맹점 로그인 아이디")
  val partnerLoginId: String? = null,
  @Transient
  @Schema(title = "가맹점 로그인 패스워드", description = "가맹점 로그인 패스워드", hidden = true)
  val partnerLoginPassword: String? = null,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "수정일시", description = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "삭제일시", description = "삭제일시", example = "2025-04-17 12:00:00.213123")
  val deleteDate: @Contextual LocalDateTime?
) : BaseModel<String>

@Serializable
@Schema(name = "simpleStoreModel", title = "가맹점", description = "가맹점 약식 정보")
data class SimpleStoreModel(
  @Schema(title = "가맹점 고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
  val franchiseCode: String? = null,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String? = null,
  @Schema(title = "대표자명", description = "대표자명")
  val ceoName: String? = null,
) : BaseModel<String>


@Serializable
@Schema(name = "storeSearchModel", title = "가맹점", description = "가맹점 매핑 용 약식 정보")
data class StoreSearchModel(
  @Schema(title = "가맹점 고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
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
  @Schema(title = "단말기 구분", description = "단말기 구분", example = "CAT, OKPOS,..." )
  val deviceType: String? = null,
) : BaseModel<String>


@Serializable
@Schema(name = "storeRegistModel", title = "가맹점", description = "가맹점 등록 요청 정보")
data class StoreRegistModel(
  @Schema(title = "고유아이디", description = "가맹점 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @Schema(title = "가맹점명", description = "가맹점명", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @Schema(title = "프랜차이즈코드", description = "프랜차이즈 코드")
  val franchiseCode: String? = null,
  @Schema(title = "지역코드", description = "지역코드")
  val zoneCode: String? = null,
  @Schema(title = "도로명, 지번 주소", description = "도로명, 지번 주소")
  val addr1: String? = null,
  @Schema(title = "상세 주소", description = "상세 주소")
  val addr2: String? = null,
  @Schema(title = "지도 url", description = "지도 url")
  val mapUrl: String? = null,
  @Schema(title = "위도 좌표", description = "위도 좌표")
  val lat: String? = null,
  @Schema(title = "경도 좌표", description = "경도 좌표")
  val lon: String? = null,
  @Schema(title = "대표자 전화번호", description = "대표자 전화번호")
  val tel: String? = null,
  @Schema(title = "담당자 번호", description = "담당자 번호")
  val mobile: String? = null,
  @Schema(title = "담당자명", description = "담당자명")
  val managerName: String? = null,
  @Schema(title = "사이트 주소", description = "사이트 주소")
  val siteLink: String? = null,
  @Schema(title = "워크타입", description = "워크타입")
  val workType: String? = null,
  @Schema(title = "사업자 등록번호", description = "사업자 등록번호")
  val businessNo: String? = null,
  @Schema(title = "법인 등록번호", description = "법인 등록번호")
  val businessNoLaw: String? = null,
  @Schema(title = "대표자명", description = "대표자명")
  val ceoName: String? = null,
  @Schema(title = "업종", description = "업종")
  val businessType: String? = null,
  @Schema(title = "업태", description = "업태")
  val eventType: String? = null,
  @Schema(title = "이메일", description = "이메일")
  val email: String? = null,
  @Schema(title = "가맹점 타입", description = "가맹점 타입")
  val storeType: String? = null,
  @Schema(title = "아이콘 URL", description = "아이콘 URL")
  val iconUrl: String? = null,
  @Schema(title = "로고 URL", description = "로고 URL")
  val logoUrl: String? = null,
  @Schema(title = "영수증 너비 인치", description = "영수증 너비 인치")
  val receiptWidthInch: String? = null,
  @Schema(title = "가맹점 상태", description = "가맹점 상태코드", example = "ACTIVE", requiredMode = RequiredMode.REQUIRED, allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"])
  val status: StoreStatus? = null,
  @Schema(title = "가맹점 로그인 아이디", description = "가맹점 로그인 아이디")
  val partnerLoginId: String? = null,
  @Transient
  @Schema(title = "가맹점 로그인 패스워드", description = "가맹점 로그인 패스워드", hidden = true)
  val partnerLoginPassword: String? = null,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "수정일시", description = "수정일시", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "삭제일시", description = "삭제일시", example = "2025-04-17 12:00:00.213123")
  val deleteDate: @Contextual LocalDateTime?
) : BaseModel<String>

object StoreTable : Table("store") {
  val id = varchar("store_uid", length = 36)
  val storeName = varchar("store_name", length = 255)
  val storeType = varchar("store_type", length = 255).nullable()
  val zoneCode = varchar("zone_code", length = 20).nullable()
  val addr1 = varchar("addr1", length = 255).nullable()
  val addr2 = varchar("addr2", length = 255).nullable()
  val regDate = datetime("reg_date").nullable()
  val deleteDate = datetime("delete_date").nullable()
  val iconUrl = varchar("icon_url", length = 255).nullable()
  val logoUrl = varchar("logo_url", length = 255).nullable()
  val franchiseCode = varchar("franchise_code", length = 30).nullable()
  val mapUrl = varchar("map_url", length = 255).nullable()
  val lat = varchar("lat", length = 20).nullable()
  val lon = varchar("lon", length = 20).nullable()
  val tel = varchar("tel", length = 15).nullable()
  val mobile = varchar("mobile", length = 15).nullable()
  val managerName = varchar("manager_name", length = 30).nullable()
  val siteLink = varchar("site_link", length = 255).nullable()
  val receiptWidthInch = varchar("receipt_width_inch", length = 2).nullable()
  val status = enumerationByName<StoreStatus>("status", 20).nullable()
  val workType = varchar("work_type", length = 30).nullable()
  val businessNo = varchar("business_no", length = 30).nullable()
  val partnerLoginId = varchar("partner_login_id", length = 50).nullable()
  val partnerLoginPassword = varchar("partner_login_pword", length = 255).nullable()
  val ceoName = varchar("ceo_name", length = 30).nullable()
  val businessType = varchar("business_type", length = 255).nullable()
  val eventType = varchar("event_type", length = 255).nullable()
  val email = varchar("email", length = 255).nullable()
  val businessNoLaw = varchar("business_no_law", length = 30).nullable()
  val modDate = datetime("mod_date").nullable()
  override val primaryKey = PrimaryKey(id)
}

@Serializable
@Schema(name = "searchStoreFilter", title = "매핑 용 가맹점 검색 필터", description = "매핑 용 가맹점 검색 필터, 예) 태그 등록 시")
data class StoreSearchFilter(
  val id: String? = null,
  @Schema(title = "사업자 번호", description="사업자 번호, EQ 검색", example="123-45-67890", requiredMode = RequiredMode.NOT_REQUIRED)
  val businessNo: String? = null,
  @Schema(title = "가맹점명", description = "Start with 검색", requiredMode = RequiredMode.NOT_REQUIRED)
  val name: String? = null,
  val franchiseCode: String? = null,
  @Schema(title = "정렬", requiredMode = RequiredMode.NOT_REQUIRED, exampleClasses = [Sorter::class], description = """
    정렬 필드 : id, businessNo, name, franchiseCode
  """)
  override val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
): BaseFilter

@Serializable
@Schema(name = "storeFilter", title = "가맹점 검색 필터", description = "가맹점 검색 필터")
data class StoreFilter(
  @Schema(title = "가맹점 아이디", description = "가맹점 고유아이디, EQ 검색",  requiredMode = RequiredMode.NOT_REQUIRED)
  val id: String? = null,
  @Schema(title = "사업자 번호", description="사업자 번호, EQ 검색", example="123-45-67890", requiredMode = RequiredMode.NOT_REQUIRED)
  val businessNo: String? = null,
  @Schema(title = "가맹점명", description = "Start with 검색", requiredMode = RequiredMode.NOT_REQUIRED)
  val name: String? = null,
  @Schema(title = "프랜차이즈 코드", description = "EQ 검색, 서비스코드 조회에서 가져온 프랜차이즈 코드", example = "EDIYA", requiredMode = RequiredMode.NOT_REQUIRED)
  val franchiseCode: String? = null,
  @Schema(title = "검색 기간", description = "검색할 기간의 시작과 끝의 범위", example = """{"from: "2025-04-17T12:00:00", "to: "2025-05-17T12:00:00"}""")
  val period: PeriodFilter,
  @Schema(title = "정렬", requiredMode = RequiredMode.NOT_REQUIRED, exampleClasses = [Sorter::class], description = """
    정렬 필드 : id, businessNo, name, franchiseCode, regDate, modDate, addr1, managerName, ceoName
  """)
  override val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
): BaseFilter

enum class StoreStatus(
  val value: String
) {
  ACTIVE("정상"),
  NORMAL("정상"),
  INACTIVE("중지"),
  PENDING("대기"),
  DELETED("삭제")
}
