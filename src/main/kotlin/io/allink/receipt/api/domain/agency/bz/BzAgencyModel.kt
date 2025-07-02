package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.BaseFilter
import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.domain.admin.AdminStatus
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

@Serializable
@Schema(name = "bzAgencyModel", title = "대리점 정보", description = "대리점 정보를 나타내는 객체, ")
data class BzAgencyModel(
  @Schema(title = "고유 아이디", description = "대리점 고유 식별자", readOnly = true)
  override var id: @Contextual UUID?,
  @Schema(title = "대리점명", description = "대리점 이름", requiredMode = RequiredMode.REQUIRED)
  val agencyName: String? = null,
  @Schema(title = "사업자번호", description = "대리점 사업자 번호", requiredMode = RequiredMode.REQUIRED)
  val businessNo: String? = null,
  @Schema(title = "주소 1", description = "대리점 주소 (상세주소가 아닌 기본주소)")
  val addr1: String? = null,
  @Schema(title = "주소 2", description = "대리점 상세 주소")
  val addr2: String? = null,
  @Schema(title = "전화번호", description = "대리점 전화번호")
  val tel: String? = null,
  @Schema(title = "대표자 이름", description = "대리점 대표자 이름")
  val ceoName: String? = null,
  @Schema(title = "대표자 전화번호", description = "대리점 대표자 전화번호")
  val ceoPhone: String? = null,
  @Schema(title = "담당자", description = "담당자(시스템 사용자)")
  val staffs: List<BzAgencyAdminModel>? = listOf(),
  @Schema(
    title = "신청서 파일 경로",
    description = "값의 유무에 따라 대리점 신청서 등록 여부 결정, null 이면 등록 안됌, 파일 업로드 후 응답 받은 값을 설정",
    example = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/application.pdf | null"
  )
  val applicationFilePath: String? = null,
  @Schema(
    title = "사업자 등록증 파일 경로",
    description = "값의 유무에 따라 사업자 등록증 파일 등록 여부 결정, null 이면 등록 안됌, 파일 업로드 후 응답 받은 값을 설정",
    example = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bz.pdf | null"
  )
  val bzFilePath: String? = null,
  @Schema(
    title = "대표자 신분증 파일 경로",
    description = "값의 유무에 따라 대표자 신분증 파일 등록 여부 결정, null 이면 등록 안됌, 파일 업로드 후 응답 받은 값을 설정",
    example = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/id.pdf"
  )
  val idFilePath: String? = null,
  @Schema(
    title = "통장사본 파일 경로",
    description = "값의 유무에 따라 통장 사본 파일 등록 여부 결정, null 이면 등록 안됌, 파일 업로드 후 응답 받은 값을 설정",
    example = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bank.pdf | null"
  )
  val bankFilePath: String? = null,
  @Schema(title = "영수증 제휴 여부", description = "영수증 제휴 상태", example = "true|false")
  val isReceiptAlliance: Boolean? = null,
  @Schema(title = "영수증 인프라 배분율", description = "영수증 관련 인프라 배분율", example = "0-100")
  val infraRatio: Int? = null,
  @Schema(title = "리워드 기본료 배분율", description = "리워드 기본료 배분율", example = "0-100")
  val rewardBaseRatio: Int? = null,
  @Schema(title = "리워드 수수료 배분율", description = "리워드 수수료 배분율", example = "0-100")
  val rewardCommissionRatio: Int? = null,
  @Schema(title = "리워드 패키지 배분율", description = "리워드 패키지 배분율 (999+)", example = "0-100")
  val rewardPackageRatio: Int? = null,
  @Schema(title = "영수증 광고 배분율", description = "영수증 광고 수익 배분율", example = "0-100")
  val advertisementRatio: Int? = null,
  @Schema(title = "핫플 쿠폰 광고 여부", description = "핫플레이스 쿠폰광고 여부")
  val isCouponAdv: Boolean? = null,
  @Schema(title = "쿠폰 광고 배분율", description = "쿠폰 광고 관련 배분율", example = "0-100")
  val couponAdvRatio: Int? = null,
  @Schema(title = "태그 예치금", description = "태그 보증금 금액")
  val tagDeposit: Int? = null,
  @Schema(title = "대리점 보증금", description = "대리점 운용 보증금")
  val agencyDeposit: Int? = null,
  @Schema(title = "은행명", description = "통장 사본에 기재된 은행명")
  val settlementBank: String? = null,
  @Schema(title = "예금주명", description = "통장 사본에 기재된 예금주 이름")
  val bankAccountName: String? = null,
  @Schema(title = "계좌번호", description = "등록된 은행 계좌번호")
  val bankAccountNo: String? = null,
  @Schema(
    title = "상태",
    description = "대리점 상태",
    requiredMode = RequiredMode.REQUIRED,
    example = "ACTIVE, INACTIVE",
    defaultValue = "ACTIVE"
  )
  val status: AgencyStatus? = null,
  @Schema(
    title = "등록 일시",
    description = "대리점 정보 등록 일시",
    requiredMode = RequiredMode.REQUIRED,
    example = "2025-05-22T12:00:00",
    readOnly = true
  )
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "등록자 ID", description = "대리점 정보 등록자 ID", requiredMode = RequiredMode.REQUIRED, readOnly = true)
  val regBy: @Contextual UUID? = null,
  @Schema(title = "수정 일시", description = "대리점 정보 최종 수정 일시", example = "2025-06-01T12:00:00", readOnly = true)
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "수정자 ID", description = "대리점 정보 수정자 ID", readOnly = true)
  val modBy: @Contextual UUID? = null,
) : BaseModel<UUID>

@Serializable
@Schema(name = "listAgencyModel", title = "대리점 정보 목록 용", description = "대리점 정보를 나타내는 목록 용 객체")
data class BzListAgencyModel(
  @Schema(title = "고유 아이디", description = "대리점 고유 식별자", requiredMode = RequiredMode.REQUIRED)
  override var id: @Contextual UUID? = null,
  @Schema(title = "대리점명", description = "대리점 이름", requiredMode = RequiredMode.REQUIRED)
  val agencyName: String?,
  @Schema(title = "사업자번호", description = "대리점 사업자 번호", requiredMode = RequiredMode.REQUIRED)
  val businessNo: String?,
  @Schema(title = "상태", description = "대리점 상태", requiredMode = RequiredMode.REQUIRED, example = "ACTIVE, INACTIVE")
  val status: AgencyStatus,
  val latestLoginAt: @Contextual LocalDateTime? = null
) : BaseModel<UUID>


@Serializable
@Schema(name = "bzAgencyAdminModel", title = "대리점 담당자", description = "대리점 담당자 (시스템 사용자)")
data class BzAgencyAdminModel(
  @Schema(title = "고유 아이디", description = "담당자 식별자, id 값이 없으면 신규 생성")
  override var id: @Contextual UUID? = null,
  @Schema(title = "담당자명", description = "담당자명", requiredMode = RequiredMode.REQUIRED)
  val fullName: String,
  @Schema(title = "전화번호", description = "전화번호: 고유한 값, 전화번호 중복 등록불가", requiredMode = RequiredMode.REQUIRED)
  val phone: String,
  @Schema(title = "이메일", description = "이메일")
  val email: String?,
  @Schema(title = "상태", description = "상태")
  val status: AdminStatus,
  @Schema(title = "등록일시", description = "등록일시")
  val regDate: @Contextual LocalDateTime? = null,
  @Schema(title = "수정일시", description = "수정일시")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "등록인", description = "등록인")
  val regBy: @Contextual UUID? = null,
  @Schema(title = "수정인", description = "수정인")
  val modBy: @Contextual UUID? = null,
) : BaseModel<UUID>

object BzAgencyTable : UUIDTable(name = "bz_agency", columnName = "uuid") {
  val agencyName = varchar(name = "agency_name", length = 255).nullable()
  val businessNo = varchar(name = "business_no", length = 20).nullable()
  val addr1 = varchar(name = "addr1", length = 255).nullable()
  val addr2 = varchar(name = "addr2", length = 255).nullable()
  val tel = varchar(name = "tel", length = 20).nullable()
  val ceoName = varchar(name = "ceo_name", length = 50).nullable()
  val ceoPhone = varchar(name = "ceo_phone", length = 20).nullable()
  val applicationFilePath = varchar(name = "application_file_path", length = 255).nullable()
  val bzFilePath = varchar(name = "bz_file_path", length = 255).nullable()
  val idFilePath = varchar(name = "id_file_path", length = 255).nullable()
  val bankFilePath = varchar(name = "bank_file_path", length = 255).nullable()
  val isReceiptAlliance = bool(name = "is_receipt_alliance").nullable()
  val infraRatio = integer(name = "infra_ratio").nullable()
  val rewardBaseRatio = integer(name = "reward_base_ratio").nullable()
  val rewardCommissionRatio = integer(name = "reward_commission_ratio").nullable()
  val rewardPackageRatio = integer(name = "reward_package_ratio").nullable()
  val advertisementRatio = integer(name = "advertisement_ratio").nullable()
  val isCouponAdv = bool(name = "is_coupon_adv").nullable()
  val couponAdvRatio = integer(name = "coupon_adv_ratio").nullable()
  val tagDeposit = integer(name = "tag_deposit").nullable()
  val agencyDeposit = integer(name = "agency_deposit").nullable()
  val settlementBank = varchar(name = "settlement_bank", length = 50).nullable()
  val bankAccountName = varchar(name = "bank_account_name", length = 50).nullable()
  val bankAccountNo = varchar(name = "bank_account_no", length = 50).nullable()
  val status = enumerationByName("status", 20, AgencyStatus::class)
  val regDate = datetime(name = "reg_date").default(nowLocalDateTime())
  val regBy = uuid(name = "reg_by")
  val modDate = datetime(name = "mod_date").nullable()
  val modBy = uuid(name = "mod_by").nullable()
}

@Serializable
@Schema(name = "bzAgencyFilter", title = "대리점 검색 필터", description = "대리점 검색 필터")
data class BzAgencyFilter(
  @Schema(title = "대리점 아이디", description = "대리점 고유아이디, EQ 검색")
  val id: String? = null,
  @Schema(
    title = "사업자 번호",
    description = "사업자 번호, EQ 검색",
    example = "123-45-67890",
    
  )
  val businessNo: String? = null,
  @Schema(title = "대리점명", description = "Start with 검색")
  val agencyName: String? = null,
  @Schema(title = "상태", description = "EQ 검색")
  val status: AgencyStatus? = null,
  @Schema(
    title = "정렬", exampleClasses = [Sorter::class], description = """
    정렬 필드 : id, agencyName, businessNo, latestLoginAt 
  """
  )
  override val sort: List<Sorter>? = null,
) : BaseFilter

@Schema(
  name = "agencyStatus",
  title = "상태코드",
  description = "상태값",
  example = "INITIAL: 초기 등록, INACTIVE: 중지, PENDING: 대기, DELETED: 삭제"
)
enum class AgencyStatus(val desc : String) {
  INITIAL("초기 등록"),
  PENDING("생성 중"),
  ACTIVE("활성화"),
  INACTIVE("비활성화")
}

