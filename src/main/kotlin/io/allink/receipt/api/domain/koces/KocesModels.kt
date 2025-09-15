package io.allink.receipt.api.domain.koces

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Payment domain models for external payment gateway integration
 * Created by: Devonshin
 * Date: 2025-07-07
 */

@Serializable @Schema(title = "결제 요청", description = "e-receipt-gate API를 통한 결제 요청")
data class KocesPayRequest(
  @param:Schema(title = "청구 일련번호", description = "결제 요청을 위한 청구 일련번호", example = "7001")
  val billingSeq: Long
)

@Serializable @Schema(title = "결제 취소 요청", description = "e-receipt-gate API를 통한 결제 취소 요청")
data class KocesCancelRequest(
  @param:Schema(title = "요청 일련번호", description = "취소할 결제의 요청 일련번호", example = "20250107001")
  val requestSeq: Long,
  @Contextual
  @param:Schema(title = "토큰 UUID", description = "결제 취소를 위한 인증 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
  val tokenUuid: UUID
)

@Serializable @Schema(title = "결제 응답", description = "결제 요청에 대한 응답")
data class KocesGateResponse(
  @param:Schema(title = "성공 여부", description = "결제 처리 성공 여부", example = "OK|NOTOK")
  val resultCode: String,
  @param:Schema(title = "응답 데이터", description = "responseSeq, message", required = false)
  val resultData: Map<String, KocesResultData>? = null,
  @param:Schema(title = "결과코드", description = "코세스 응답 코드", required = false, example = "0000|1234")
  val resultMessage: String? = null,
  @param:Schema(title = "에러 메시지", description = "에러 메시지", required = false)
  val errorMessage: String? = null,
  @param:Schema(title = "응답 코드", description = "", required = false)
  val errorCode: String? = null
)

@Serializable
data class KocesResultData(
  val responseSeq: Long,
  val message: String
)