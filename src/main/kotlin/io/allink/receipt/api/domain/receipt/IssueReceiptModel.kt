package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.*
import io.allink.receipt.api.domain.advertisement.AdvertisementTable
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.domain.user.review.UserReviewStatus
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 17/04/2025
 */


@Serializable @Schema(
  name = "issueReceipt",
  title = "영수증",
  description = "발행된 영수증",
  nullable = false,
  requiredMode = RequiredMode.REQUIRED
)
data class IssueReceiptModel(
  @param:Schema(
    title = "영수증 고유아이디",
    description = "영수증 발행 시 자체 발행된 고유 아이디",
    example = "3a931370-cd0b-4427-bf38-418111969c22",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String?,
  @param:Schema(title = "가맹점", description = "영수증 발행 가맹점", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val store: SimpleStoreModel?,
  @param:Schema(title = "태그", description = "영수증 발행 시 태깅한 태그", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val tag: SimpleMerchantTagReceiptModel?,
  @param:Schema(
    title = "등록일시",
    description = "등록일시",
    example = "2025-04-17 12:00:00",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  val issueDate: @Contextual LocalDateTime,
  @param:Schema(title = "사용자", description = "사용자", nullable = true)
  val user: SimpleUserModel?,
  @param:Schema(title = "영수증 발행 타입", description = "환불, 결제", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptType: String,
  @param:Schema(title = "결제 금액", description = "영수증에 표시된 결제 금액", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptAmount: Int,
  @Schema(title = "영수증 원본 고유아이디", description = "영수증 발행 시 원본 고유 아이디")
  val originIssueId: String?,
  @Schema(title = "유저 리뷰", description = "유저 영수증 리뷰")
  val userPointReview: SimpleUserPointReviewModel?,
  @Schema(title = "유저 리뷰", description = "유저 영수증 리뷰")
  var edoc: SimpleEdocModel? = null,
  @Schema(title = "광고", description = "영수증 발행 시 연결된 광고")
  val advertisement: SimpleAdvertisementModel?
) : BaseModel<String>

@Serializable
@Schema(
  name = "simpleIssueReceipt",
  title = "영수증",
  description = "발행된 영수증 약시 정보",
  nullable = false,
  requiredMode = RequiredMode.REQUIRED
)
data class SimpleIssueReceiptModel(
  @Schema(
    title = "영수증 고유아이디",
    description = "영수증 발행 시 자체 발행된 고유 아이디",
    example = "3a931370-cd0b-4427-bf38-418111969c22",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  override var id: String?,
  @Schema(title = "가맹점", description = "영수증 발행 가맹점", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val store: SimpleStoreModel,
  @Schema(
    title = "태그 고유아이디",
    description = "영수증 발행 시 태깅한 태그의  고유아이디",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  val tagId: String?,
  @Schema(
    title = "등록일시",
    description = "등록일시",
    example = "2025-04-17 12:00:00",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  val issueDate: @Contextual LocalDateTime,
  @Schema(title = "사용자", description = "사용자", nullable = true)
  val user: SimpleUserModel,
  @Schema(title = "영수증 발행 타입", description = "환불, 결제", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptType: String,
  @Schema(title = "결제 금액", description = "영수증에 표시된 결제 금액", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptAmount: Int,
  @Schema(
    title = "영수증 원본 고유아이디",
    description = "영수증 발행 시 원본 고유 아이디",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED
  )
  val originIssueId: String?
) : BaseModel<String>


object IssueReceiptTable : Table("receipt_issue") {
  val id = varchar("issued_receipt_uid", length = 36)
  val tagId = reference("tag_id", MerchantTagTable.id).nullable()
  val storeUid = reference("store_uid", StoreTable.id).nullable()
  val issueDate = datetime("issue_date")
  val userUid = reference("user_uid", UserTable.id).nullable()
  val receiptType = varchar("receipt_type", length = 20)
  val receiptAmount = integer("receipt_amount")
  val originIssueId = varchar("origin_issue_id", length = 36).nullable()
  val advertisementId = reference("advertisement_uuid", AdvertisementTable.id).nullable()
  override val primaryKey: PrimaryKey? = PrimaryKey(id)
}
/*

object IssueReceiptTraceTable: Table("receipt_issue_trace") {
  val id = varchar("issued_receipt_uid", length = 36)
  val tagId = reference("tag_id", MerchantTagTable.id)
  val storeUid = reference("store_uid", StoreTable.id)
  val issueDate = datetime("issue_date")
  val userUid = reference("user_uid", UserTable.id)
  val receiptType = varchar("receipt_type", length = 20)
  val receiptAmount = integer("receipt_amount" )
  val originIssueId = varchar("origin_issue_id", length = 36).nullable()
  val userReviewId = reference("issued_receipt_uid", UserPointReviewTable.id).nullable()
  val advertisementId = reference("advertisement_uuid", AdvertisementTable.id).nullable()
  override val primaryKey: PrimaryKey? = PrimaryKey(id)
}
*/

@Serializable
@Schema(title = "검색 필터", description = "검색용 필터")
data class ReceiptFilter(
  @Schema(
    title = "검색 기간",
    description = "검색할 기간의 시작과 끝의 범위",
    example = """{"from: "2025-04-17 12:00:00", "to: "2025-05-17 12:00:00"}""",
    requiredMode = RequiredMode.REQUIRED
  )
  val period: PeriodFilter,
  @Schema(title = "휴대폰 번호", description = "휴대폰 번호, EQ 검색", nullable = false, example = "01012349876")
  val phone: String? = null,
  @Schema(title = "사용자 고유아이디", description = "사용자 고유아이디, EQ 검색", nullable = false)
  val userId: String? = null,
  @Schema(title = "이름", description = "사용자 이름, Start with 검색", nullable = false)
  val userName: String? = null,
  @Schema(title = "닉네임", description = "소셜 연동 닉네임, EQ 검색")
  val userNickName: String? = null,
  @Schema(title = "태그아이디", description = "태그의 고유아이디 EQ 검색", example = "E001234567890", nullable = false)
  val tagUid: String? = null,
  @Schema(title = "가맹점 아이디", description = "가맹점 고유아이디, EQ 검색")
  val storeId: String? = null,
  @Schema(title = "사업자 번호", description = "사업자 번호, EQ 검색", example = "123-45-67890")
  val businessNo: String? = null,
  @Schema(title = "가맹점명", description = "Start with 검색")
  val storeName: String? = null,
  @Schema(title = "프랜차이즈 코드", description = "서비스코드 조회에서 가져온 프랜차이즈 코드, EQ 검색", example = "EDIYA")
  val franchiseCode: String? = null,
  @Schema(
    title = "정렬", exampleClasses = [Sorter::class], ref = "", description = """
    정렬 필드: phone, userId, userName, userNickName, tagUid, storeId, storeBusinessNo, storeName, franchiseCode, issueDate, receiptType, receiptAmount
  """, example = "{\"field\": \"issueDate\", \"direction\": \"DESC\"}"
  )
  override val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
) : BaseFilter


@Serializable
@Schema(name = "simpleMerchantTagModelOfReceipt", title = "머천트 태그", description = "실물 태그의 약식 등록 정보")
data class SimpleMerchantTagReceiptModel(
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

@Serializable
@Schema(title = "사용자 객체", description = "모바일 전자영수증 가입자 약식 정보")
data class SimpleUserModel(
  @Schema(title = "고유아이디", description = "사용자 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "이름", description = "사용자 이름", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val name: String?,
) : BaseModel<String>


@Serializable
@Schema(name = "SimpleAdvertisementModel", title = "광고")
data class SimpleAdvertisementModel(
  @Schema(title = "광고주 머천트 고유아이디", description = "광고주 머천트 고유아이디")
  val merchantGroupId: String?,
  @Schema(title = "광고주 타이틀", description = "광고 타이틀")
  val title: String?,
  override var id: @Contextual UUID? = null
) : BaseModel<UUID>


@Serializable
@Schema(name = "SimpleUserPointReviewModel", title = "사용자 포인트 리뷰", description = "사용자의 포인트 리뷰 객체")
data class SimpleUserPointReviewModel(
  @Schema(
    title = "사용자 포인트 리뷰 고유아이디",
    description = "사용자의 포인트 리뷰 고유아이디",
    requiredMode = Schema.RequiredMode.REQUIRED,
    example = "3a931370-cd0b-4427-bf38-418111969c22"
  )
  override var id: String?,
  @Schema(title = "현재 상태", description = "리뷰 작성 상태", requiredMode = RequiredMode.REQUIRED)
  val status: UserReviewStatus?
) : BaseModel<String>


@Serializable
@Schema(name = "EdocModel", title = "전자문서", description = "전자문서 간단 발송 정보")
class SimpleEdocModel(
  @Schema(
    title = "전자문서 발송기관",
    description = "전자문서 발송기관 코드",
    requiredMode = RequiredMode.REQUIRED,
    example = "kakao|naver"
  )
  override var id: String?,
  @Schema(title = "전자문서 아이디", description = "전자문서 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val envelopId: String,
  @Schema(title = "발송일시", description = "전자문서 발송 요청일시", requiredMode = RequiredMode.REQUIRED)
  val regDate: @Contextual LocalDateTime,
) : BaseModel<String>