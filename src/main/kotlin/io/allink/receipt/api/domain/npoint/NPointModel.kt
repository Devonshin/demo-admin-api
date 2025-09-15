package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.*
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.point
 * Created: Devonshin
 * Date: 20/05/2025
 */

@Serializable @Schema(name = "nPointPayModel", title = "포인트 지급 내역 객체", description = "포인트 지급 내역 정보 객체")
data class NPointPayModel(
  @param:Schema(title = "고유 아이디", description = "지급 이력 고유 아이디 (시퀀스)", requiredMode = RequiredMode.REQUIRED)
  override var id: Long?,
  @param:Schema(title = "포인트 적립 금액", description = "포인트 적립 금액", requiredMode = RequiredMode.REQUIRED)
  val point: Int?,
  @param:Schema(title = "포인트 적립 상태", description = "포인트 적립 연동 결과 값")
  val status: String?,
  @param:Schema(title = "사용자", description = "포인트 적립 사용자", requiredMode = RequiredMode.REQUIRED)
  val user: NPointUserModel,
  @param:Schema(title = "가맹점", description = "포인트 제공 가맹점", requiredMode = RequiredMode.REQUIRED)
  val store: SimpleStoreModel,
  @param:Schema(title = "적립 케이스", description = "포인트 적립 케이스", example = "리뷰|이벤트", requiredMode = RequiredMode.REQUIRED)
  val provideCase: String,
  @param:Schema(title = "포인트 거래번호", description = "포인트 거래번호", requiredMode = RequiredMode.REQUIRED)
  val pointTrNo: String?,
  @param:Schema(title = "적립 거래번호", description = "적립 거래번호", requiredMode = RequiredMode.REQUIRED)
  val pointPayNo: String?,
  @param:Schema(title = "적립 거래번호", description = "적립 거래번호", requiredMode = RequiredMode.REQUIRED)
  val regDate: @Contextual LocalDateTime?
) : BaseModel<Long>

@Serializable @Schema(name = "pointUserModel", title = "사용자 객체", description = "모바일 전자영수증 가입자")
data class NPointUserModel(
  @param:Schema(title = "고유아이디", description = "사용자 고유아이디", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @param:Schema(title = "이름", description = "사용자 이름", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val name: String?,
  @param:Schema(
    title = "휴대폰번호",
    description = "휴대폰번호",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED,
    example = "01012349876"
  )
  val phone: String?,
  @param:Schema(title = "성별", description = "성별", nullable = false, requiredMode = RequiredMode.REQUIRED, example = "F|M")
  val gender: String?,
  @Schema(title = "생년월일", description = "생년월일", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val birthday: String?,
  @Schema(title = "닉네임", description = "소셜 연동 닉네임")
  val nickname: String?,
) : BaseModel<String>

data class NPointTxtHistoryModel(
  val txNo: String,
  val regDate: LocalDateTime,
  val resultCode: String,
  override var id: Long?
) : BaseModel<Long>

object NPointWaitingTable : Table("n_point_pay_waiting") {
  val id = long("seq")
  val receiptUuid = reference("receipt_uuid", NPointUserReviewTable.id)
  val provideCase = enumerationByName<PointProvideCase>("provide_case", length = 10)
  val regDate = datetime("reg_date")
  override val primaryKey = PrimaryKey(id)
}

object NPointUserReviewTable : Table("n_point_user_review") {
  val id = varchar("receipt_uuid", length = 36)
  val userUuid = reference("user_uuid", UserTable.id)
  val storeUid = reference("store_uid", StoreTable.id)
  val status = varchar("status", length = 10)
  val reviewUrl = varchar("review_url", length = 255).nullable()
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
  val points = integer("points").nullable()
  val expireDate = datetime("expire_date").nullable()
  override val primaryKey = PrimaryKey(id)
}

object UserEventPointTable : Table("user_event_point") {
  val id = reference("waiting_seq", NPointWaitingTable.id)
  val userUuid = varchar("user_uuid", length = 36)
  val eventSessionId = varchar("event_session_id", length = 36)
  val transactionId = varchar("transaction_id", length = 36)
  val receiptUuid = varchar("receipt_uuid", length = 36)
  val points = integer("points").nullable()
  val advertisementTitle = varchar("advertisement_title", length = 255)
  val reg_date = datetime("reg_date")
  override val primaryKey = PrimaryKey(id)
}

object NPointTxHistoryTable : Table("n_point_tx_history") {
  val id = reference("waiting_seq", NPointWaitingTable.id)
  val txNo = varchar("tx_no", length = 36)
  val regDate = datetime("reg_date")
  val resultCode = varchar("result_code", length = 10)
  override val primaryKey = PrimaryKey(id)
}

@Serializable
@Schema(name = "nPointFilter", title = "검색 필터", description = "검색용 필터")
data class NPointFilter(
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
    정렬 필드: phone, userId, userName, userNickName, storeId, storeBusinessNo, storeName, franchiseCode, regDate
  """, example = "{\"field\": \"regDate\", \"direction\": \"DESC\"}"
  )
  override val sort: List<Sorter>? = null,
  @Schema(title = "페이징", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
) : BaseFilter


@Schema(title = "포인트 지급 경로", description = "포인트를 지급하는 사유")
enum class PointProvideCase(val desc: String) {
  EVENT("이벤트"),
  REVIEW("리뷰");

  companion object {
    fun from(value: String): PointProvideCase {
      return try {
        valueOf(value.uppercase())
      } catch (e: IllegalArgumentException) {
        throw IllegalStateException("Unknown PointProvideCase: $value")
      }
    }
  }
}