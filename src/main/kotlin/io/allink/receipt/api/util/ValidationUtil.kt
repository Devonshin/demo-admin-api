/**
 * @file ValidationUtil.kt
 * @brief 유효성 검증 관련 유틸리티 함수
 * @author Devonshin
 * @date 2025-08-06
 */
package io.allink.receipt.api.util

/**
 * @param businessNo 검증할 사업자등록번호 문자열. 하이픈(-)을 포함할 수 있습니다.
 * @return 사업자등록번호가 유효하면 `true`, 그렇지 않으면 `false`를 반환합니다.
 */
fun isValidBusinessNo(businessNo: String): Boolean {
  val cleaned = businessNo.replace(Regex("\\D"), "")

  if (cleaned.length != 10) {
    return false
  }

  // 모든 자리가 0인 경우 무효 처리
  if (cleaned == "0000000000") {
    return false
  }

  val digits = cleaned.map { it.digitToIntOrNull() ?: return false }
  val weights = listOf(1, 3, 7, 1, 3, 7, 1, 3, 5)
  var sum = 0
  for (i in 0..7) {
    sum += digits[i] * weights[i]
  }

  val ninthProduct = digits[8] * weights[8]
  sum += ninthProduct / 10
  sum += ninthProduct % 10

  val checkDigit = (10 - (sum % 10)) % 10

  return digits[9] == checkDigit
}

