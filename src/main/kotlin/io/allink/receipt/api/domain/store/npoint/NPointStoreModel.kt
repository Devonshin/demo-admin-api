package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

@Serializable
@Schema(name = "nPointStore", title = "NPoint 가맹점 정보", description = "NPoint 가맹점 정보")
data class NPointStoreModel(
  @Schema(title = "가맹점 고유아이디", description = "가맹점 고유아이디")
  override var id: String? = null,
  @Schema(title = "적립 포인트", description = "포인트 지급 용 적립 포인트", defaultValue = "0")
  var reservedPoints: Int? = 0,
  @Schema(title = "리뷰 포인트", description = "리뷰 작성 시 지급할 포인트", defaultValue = "0")
  var reviewPoints: Int = 0,
  @Schema(title = "누적 적립 포인트", description = "누적 적립된 포인트", defaultValue = "0")
  var cumulativePoints: Int = 0,
  @Schema(title = "정기결제 요금액", description = "정기결제 요금액")
  var regularPaymentAmounts: Int = 0,
  @Schema(title = "상태", description = "계약 상태 - PENDING 상태로 기본값 설정")
  val status: StatusCode = StatusCode.PENDING,
  @Schema(title = "서비스 시작일시", description = "서비스 시작일시")
  var serviceStartAt: @Contextual LocalDateTime? = null,
  @Schema(title = "서비스 종료일시", description = "서비스 종료일시")
  var serviceEndAt: @Contextual LocalDateTime? = null,
  @Schema(title = "포인트 소진 시 갱신 타입", description = "포인트 소진 시 적용할 갱신 타입 (기본값: AUTO_RENEWAL)")
  var pointRenewalType: PointRenewalType = PointRenewalType.AUTO_RENEWAL,
  @Schema(title = "등록일시", description = "계약 생성일", defaultValue = "CURRENT_TIMESTAMP")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", description = "계약 정보 수정일")
  var modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "등록자 고유아이디", description = "계약 정보를 등록한 사람의 고유아이디")
  val regBy: @Contextual UUID,
  @Schema(title = "수정자 고유아이디", description = "계약 정보를 수정한 사람의 고유아이디")
  var modBy: @Contextual UUID? = null,
) : BaseModel<String>

enum class PointRenewalType {
  AUTO_RENEWAL,
  MANUAL_RENEWAL,
  NON_RENEWAL
}

object NPointStoreTable : Table("n_point_store") {

  val id = varchar("store_uid", 36) // 가맹점 고유아이디
  val reservedPoints = integer("reserved_points").nullable() // 지급 용 적립 포인트
  val reviewPoints = integer("review_points").default(0) // 리뷰 작성 시 제공할 포인트
  val cumulativePoints = integer("cumulative_points").default(0) // 누적 적립 포인트
  val regularPaymentAmounts = integer("regular_payment_amounts") // 정기결제 요금액
  val status = enumerationByName<StatusCode>("status", 20) // 상태
  val serviceStartAt = datetime("service_start_at").nullable() // 서비스 시작일시
  val serviceEndAt = datetime("service_end_at").nullable() // 서비스 종료일시
  val pointRenewalType = enumerationByName<PointRenewalType>("point_renewal_type", 20) // 지급할 포인트 소진 시 갱신 타입
  val regDate = datetime("reg_date") // 등록일시
  val modDate = datetime("mod_date").nullable() // 수정일시
  val regBy = uuid("reg_by") // 등록인 고유아이디
  val modBy = uuid("mod_by").nullable() // 수정인 고유아이디

  override val primaryKey = PrimaryKey(id, name = "pk_n_point_store") // 기본 키

}



