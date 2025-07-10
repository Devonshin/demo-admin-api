package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.code.ServiceCodeTable
import io.allink.receipt.api.domain.store.StoreTable
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
@Schema(name = "nPointStoreServiceModel", title = "NPoint 서비스 정보", description = "NPoint 가맹점 관련 서비스 정보")
data class NPointStoreServiceModel(
  @Schema(title = "가맹점 서비스 등록 아이디", description = "가맹점 서비스 등록 아이디")
  override var id: NPointStoreServiceId?,
  @Schema(title = "연결된 서비스 정보", description = "연결된 서비스 정보")
  val service: ServiceCodeModel? = null,
  @Schema(title = "서비스 기본료", description = "서비스 기본 요금")
  val serviceCharge: Int,
  @Schema(title = "보증금", description = "서비스 이용을 위한 보증금")
  val rewardDeposit: Int? = null,
  @Schema(title = "지급 포인트", description = "서비스에 따라 지급된 포인트")
  val rewardPoint: Int? = null,
  @Schema(title = "수수료", description = "서비스 수수료")
  val serviceCommission: Int? = null,
  @Schema(title = "상태", description = "서비스 상태")
  val status: StatusCode = StatusCode.PENDING,
  @Schema(title = "등록일시", description = "서비스 등록 일시")
  val regDate: @Contextual LocalDateTime,
  @Schema(title = "수정일시", description = "서비스 수정 일시")
  val modDate: @Contextual LocalDateTime? = null,
  @Schema(title = "등록자 고유아이디", description = "서비스 등록을 한 사람의 고유아이디")
  val regBy: @Contextual UUID,
  @Schema(title = "수정자 고유아이디", description = "서비스 수정을 한 사람의 고유아이디")
  val modBy: @Contextual UUID? = null,
): BaseModel<NPointStoreServiceId>

@Serializable
@Schema(name = "nPointStoreServiceModifyModel", title = "NPoint 서비스 등록/수정 정보", description = "NPoint 가맹점 관련 서비스 정보")
data class NPointStoreServiceRegistModel(
  @Schema(title = "서비스 코드", description = "서비스 고유 코드", example = "광고:ADVERTIZE, 전자영수증:ERECEIPT, 네이버 999+리뷰:REVIEWPRJ, 네이버 리뷰 리워드: REVIEWPT, 배달리뷰 리워드: DLVRVIEWPT")
  val serviceCode: String,
  @Schema(title = "기본료", description = "기본 요금")
  val serviceCharge: Int? = 0,
  @Schema(title = "보증금", description = "포인트 보증금")
  val rewardDeposit: Int? = null,
  @Schema(title = "리워드 포인트", description = "지급할 포인트")
  val rewardPoint: Int? = null,
  @Schema(title = "리워드 수수료", description = "서비스 리워드 수수료")
  val serviceCommission: Int? = null
)

object NPointStoreServiceTable : Table("n_point_store_service") {
  val id = integer( "store_service_seq")
  val storeUid = reference("store_uid", StoreTable.id) // 가맹점
  val serviceCode = reference("service_code", ServiceCodeTable.serviceCode) // 서비스 아이디
  val serviceCharge = integer("service_charge") // 서비스 기본료
  val rewardDeposit = integer("reward_deposit").nullable() // 보증금
  val rewardPoint = integer("reward_point").nullable() // 지급 포인트
  val serviceCommission = integer("service_commission").nullable() // 수수료
  val status = enumerationByName<StatusCode>("status", 10)
  val regDate = datetime("reg_date") // 등록일시
  val modDate = datetime("mod_date").nullable() // 수정일시
  val regBy = uuid("reg_by") // 등록인 고유아이디
  val modBy = uuid("mod_by").nullable() // 수정인 고유아이디

  override val primaryKey = PrimaryKey(id, storeUid, serviceCode, name = "n_point_store_service_pkey")
}

@Serializable
data class NPointStoreServiceId (
  @Schema(title = "가맹점 서비스 등록 순번", description = "가맹점 서비스 등록 순번")
  val storeServiceSeq: Int,
  @Schema(title = "가맹점 고유아이디", description = "가맹점 고유아이디")
  val storeUid: String,
  @Schema(title = "서비스 코드", description = "서비스 고유 코드", example = "광고:ADVERTIZE, 전자영수증:ERECEIPT, 네이버 999+리뷰:REVIEWPRJ, 네이버 리뷰 리워드: REVIEWPT, 배달리뷰 리워드: DLVRVIEWPT")
  val serviceCode: String,
)
