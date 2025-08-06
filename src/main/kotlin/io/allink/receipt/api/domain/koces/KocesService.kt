package io.allink.receipt.api.domain.koces

import java.util.*

/**
 * Payment Gateway interface for external payment system integration
 * Created by: DevOps Team
 * Date: 2025-01-07
 */
interface KocesService {
  /**
   * 결제 요청 처리
   * @param billingSeq 청구 일련번호
   * @return 결제 처리 결과
   */
  suspend fun requestPayment(billingSeq: Long): KocesGateResponse

  /**
   * 결제 취소 처리
   * @param requestSeq 요청 일련번호
   * @param tokenUuid 인증 토큰
   * @return 취소 처리 결과
   */
  suspend fun cancelPayment(requestSeq: Long, tokenUuid: UUID): KocesGateResponse
}
