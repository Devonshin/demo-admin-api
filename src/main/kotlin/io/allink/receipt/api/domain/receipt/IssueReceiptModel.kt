package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.BaseModel
import io.allink.receipt.api.common.Page
import io.allink.receipt.api.common.Sorter
import io.allink.receipt.api.domain.merchant.MerchantTagModel
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import io.allink.receipt.api.domain.merchant.SimpleMerchantTagModel
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.store.StoreModel
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.SimpleUserModel
import io.allink.receipt.api.domain.user.UserModel
import io.allink.receipt.api.domain.user.UserTable
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 17/04/2025
 */


@Serializable
@Schema(name = "issueReceipt", title = "영수증", description = "발행된 영수증", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class IssueReceiptModel(
  @Schema(title = "영수증 고유아이디", description = "영수증 발행 시 자체 발행된 고유 아이디", example = "3a931370-cd0b-4427-bf38-418111969c22", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "가맹점", description = "영수증 발행 가맹점", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val store: StoreModel,
  @Schema(title = "태그", description = "영수증 발행 시 태깅한 태그", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val tag: MerchantTagModel,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val issueDate: @Contextual LocalDateTime,
  @Schema(title = "사용자", description = "사용자", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val user: UserModel,
  @Schema(title = "영수증 발행 타입", description = "환불, 결제", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptType: String,
  @Schema(title = "결제 금액", description = "영수증에 표시된 결제 금액", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptAmount: Int,
  @Schema(title = "영수증 원본 고유아이디", description = "영수증 발행 시 원본 고유 아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val originIssueId: String?
) : BaseModel<String>

@Serializable
@Schema(name = "simpleIssueReceipt", title = "영수증", description = "발행된 영수증 약시 정보", nullable = false, requiredMode = RequiredMode.REQUIRED)
data class SimpleIssueReceiptModel(
  @Schema(title = "영수증 고유아이디", description = "영수증 발행 시 자체 발행된 고유 아이디", example = "3a931370-cd0b-4427-bf38-418111969c22", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @Schema(title = "가맹점", description = "영수증 발행 가맹점", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val store: SimpleStoreModel,
  @Schema(title = "태그 고유아이디", description = "영수증 발행 시 태깅한 태그의  고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val tagId: String,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val issueDate: @Contextual LocalDateTime,
  @Schema(title = "사용자", description = "사용자", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val user: SimpleUserModel,
  @Schema(title = "영수증 발행 타입", description = "환불, 결제", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptType: String,
  @Schema(title = "결제 금액", description = "영수증에 표시된 결제 금액", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val receiptAmount: Int,
  @Schema(title = "영수증 원본 고유아이디", description = "영수증 발행 시 원본 고유 아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val originIssueId: String?
) : BaseModel<String>


object IssueReceiptTable: Table("receipt_issue") {
  val id = varchar("issued_receipt_uid", length = 36)
  val tagId = reference("tag_id", MerchantTagTable.id)
  val storeUid = reference("store_uid", StoreTable.id)
  val issueDate = datetime("issue_date")
  val userUid = reference("user_uid", UserTable.id)
  val receiptType = varchar("receipt_type", length = 20)
  val receiptAmount = integer("receipt_amount" )
  val originIssueId = varchar("origin_issue_id", length = 36).nullable()
  override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

@Serializable
@Schema(title = "검색 필터", description = "검색용 필터")
data class ReceiptFilter(
  @Schema(title = "검색 기간", description = "검색할 기간의 시작과 끝의 범위", example = """{"from: "2025-04-17 12:00:00", "to: "2025-05-17 12:00:00"}""", requiredMode = RequiredMode.REQUIRED)
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
  @Schema(title = "가맹점 아이디", description = "가맹점 고유아이디, EQ 검색",)
  val storeId: String? = null,
  @Schema(title = "사업자 번호", description="사업자 번호, EQ 검색", example="1234567890")
  val businessNo: String? = null,
  @Schema(title = "가맹점명", description = "Start with 검색")
  val storeName: String? = null,
  @Schema(title = "프랜차이즈 코드", description = "서비스코드 조회에서 가져온 프랜차이즈 코드, EQ 검색", example = "EDIYA")
  val franchiseCode: String? = null,
  @Schema(title = "정렬", exampleClasses = [Sorter::class])
  val sort: List<Sorter>? = null,
  @Schema(title = "페이징")
  val page: Page = Page(1, 10)
)

@Serializable
@Schema(title = "검색 기간", description = "검색할 기간의 시작과 끝의 범위", example = """{"from: "2025-04-17 12:00:00", "to: "2025-05-17 12:00:00"}""", requiredMode = RequiredMode.REQUIRED)
data class PeriodFilter(
  @Schema(title = "시작일시", description = "검색을 시작할 년월일시", example = "2025-04-17 12:00:00", requiredMode = RequiredMode.REQUIRED)
  val from: @Contextual LocalDateTime,
  @Schema(title = "종료일시", description = "검색을 종료할 년월일시", example = "2025-04-17 12:00:00", requiredMode = RequiredMode.REQUIRED)
  val to: @Contextual LocalDateTime
)





