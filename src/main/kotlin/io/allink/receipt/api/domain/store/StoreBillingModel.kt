package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 08/06/2025
 */

@Serializable
@Schema(title = "가맹점 결제 정보")
data class StoreBillingModel(
  @Schema(title = "결제 순번", description = "가맹점 등록 시 생성되는 결제 순번")
  override var id: Long? = null,
  @Schema(title = "가맹점 고유아이디", description = "가맹점 고유아이디")
  val storeUid: String,
  @Schema(title = "가맹점 서비스 등록 순번", description = "가맹점 서비스 등록 순번")
  val storeServiceSeq: Int,
  @Schema(title = "결제 토큰 고유아이디", description = "결제 토큰 고유아이디")
  val tokenUuid: @Contextual UUID,
  @Schema(title = "결제 진행 상태", description = "결제 진행 상태", defaultValue = "PENDING", example = "PENDING|COMPLETE|FAIL")
  val status: BillingStatusCode? = BillingStatusCode.PENDING,
  @Schema(title = "오늘 결제 금액", description = "오늘 결제 총액")
  val billingAmount: Int? = 0,
  @Schema(title = "환불 계좌 은행 코드", description = "환불 계좌 은행 코드, 은행 코드 목록 조회에서 확인 가능")
  val bankCode: String? = null,
  @Schema(title = "환불 계좌번호", description = "환불 계좌번호")
  val bankAccountNo: String? = null,
  @Schema(title = "계좌 예금주", description = "계좌 예금주")
  val bankAccountName: String? = null,
  @Schema(title = "등록일시", description = "등록일시", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "등록인 아이디", description = "등록인 아이디")
  val regBy: @Contextual UUID,
) : BaseModel<Long>

@Serializable
@Schema(title = "가맹점 결제 정보 등록")
data class StoreBillingRegistModel(
  @Contextual
  @Schema(title = "결제 토큰 고유아이디", description = "결제 토큰 고유아이디")
  val tokenUuid: UUID,
  @Schema(
    title = "오늘 결제 금액",
    description = "오늘 결제 총액: (월 기본 결제 금액) * (이달에 남은 날 수 + 1) / (이달 일 수)",
    example = "10000"
  )
  var billingAmount: Int = 0,
  @Schema(
    title = "결제 상태코드",
    description = "가맹점 등록 시 STANDBY 자동 설정됌, 즉시 결제 시도. 가맹점 수정 시 STANDBY(즉시결제)나 PENDING(익월 1일 결제). 결제가 성공하면 COMPLETE, 실패 시 FAIL",
    example = "PENDING: 대기(익월 1일 결제 대상), STANDBY: 즉시 결제"
  )
  val status: BillingStatusCode? = BillingStatusCode.PENDING,
  @Schema(title = "환불 계좌 은행 코드", description = "환불 계좌 은행 코드, 은행 코드 목록 조회에서 확인 가능")
  val bankCode: String? = null,
  @Schema(title = "환불 계좌번호", description = "환불 계좌번호")
  val bankAccountNo: String? = null,
  @Schema(title = "계좌 예금주", description = "계좌 예금주")
  val bankAccountName: String? = null,
)


object StoreBillingTable : LongIdTable("store_billing", "billing_seq") {
  val storeUid = varchar("store_uid", 36)
  val storeServiceSeq = integer("store_service_seq")
  val tokenUuid: Column<UUID> = uuid("token_uuid")
  val billingAmount: Column<Int?> = integer("billing_amount").nullable()
  val status: Column<BillingStatusCode> = enumerationByName<BillingStatusCode>(name = "status", length = 10)
  val bankCode: Column<String?> = varchar("bank_code", 32).nullable()
  val bankAccountNo: Column<String?> = varchar("bank_account_no", 32).nullable()
  val bankAccountName: Column<String?> = varchar("bank_account_name", 32).nullable()
  val regBy: Column<UUID> = uuid("reg_by")
  val regDate: Column<LocalDateTime> = datetime("reg_date")

  init {
    index(false, storeUid, storeServiceSeq)
  }
}