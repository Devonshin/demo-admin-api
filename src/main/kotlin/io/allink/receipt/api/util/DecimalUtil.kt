/**
 * @file DecimalUtil.kt
 * @brief 소수점 관련 변환 및 계산을 처리하는 유틸리티 클래스
 * @author Devonshin
 * @date 2025-08-21
 */
package io.allink.receipt.api.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 소수점 처리를 위한 유틸리티 클래스
 *
 * BigDecimal을 사용하여 정확한 소수점 계산을 수행합니다.
 * 금융 거래나 정밀한 계산이 필요한 곳에서 사용합니다.
 */
object DecimalUtil {
    
    /**
     * 문자열을 BigDecimal로 변환
     * 
     * @param value 변환할 문자열
     * @param defaultValue 변환 실패 시 반환할 기본값 (기본값: BigDecimal.ZERO)
     * @return 변환된 BigDecimal 또는 기본값
     */
    fun toBigDecimal(value: String?, defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal {
        if (value.isNullOrBlank()) return defaultValue
        
        return try {
            // 쉼표 제거 후 변환
            val cleanValue = value.replace(",", "").trim()
            BigDecimal(cleanValue)
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
    
    /**
     * 숫자를 지정된 소수점 자리수로 반올림
     * 
     * @param value 반올림할 값
     * @param scale 소수점 자리수
     * @param roundingMode 반올림 모드 (기본값: HALF_UP - 일반적인 반올림)
     * @return 반올림된 BigDecimal
     */
    fun round(
        value: BigDecimal, 
        scale: Int = 2, 
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): BigDecimal {
        return value.setScale(scale, roundingMode)
    }
    
    /**
     * 두 BigDecimal 값을 안전하게 더하기
     * 
     * @param a 첫 번째 값
     * @param b 두 번째 값
     * @return 더한 결과
     */
    fun add(a: BigDecimal?, b: BigDecimal?): BigDecimal {
        val safeA = a ?: BigDecimal.ZERO
        val safeB = b ?: BigDecimal.ZERO
        return safeA.add(safeB)
    }
    
    /**
     * 여러 BigDecimal 값들의 합계 계산
     * 
     * @param values 더할 값들
     * @return 합계
     */
    fun sum(vararg values: BigDecimal?): BigDecimal {
        return values.fold(BigDecimal.ZERO) { acc, value ->
            add(acc, value)
        }
    }
    
    /**
     * 백분율 계산
     * 
     * @param value 계산할 값
     * @param percentage 백분율 (예: 10은 10%를 의미)
     * @param scale 결과의 소수점 자리수
     * @return 백분율이 적용된 값
     */
    fun calculatePercentage(
        value: BigDecimal, 
        percentage: BigDecimal, 
        scale: Int = 2
    ): BigDecimal {
        val percentageDecimal = percentage.divide(BigDecimal(100))
        return round(value.multiply(percentageDecimal), scale)
    }
    
    /**
     * 금액을 포맷팅된 문자열로 변환
     * 
     * @param value 포맷팅할 값
     * @param scale 소수점 자리수
     * @param includeComma 천 단위 구분 기호 포함 여부
     * @return 포맷팅된 문자열
     */
    fun format(
        value: BigDecimal, 
        scale: Int = 2, 
        includeComma: Boolean = true
    ): String {
        val rounded = round(value, scale)
        val formatted = if (scale > 0) {
            String.format("%.${scale}f", rounded)
        } else {
            rounded.toBigInteger().toString()
        }
        
        return if (includeComma) {
            addCommas(formatted)
        } else {
            formatted
        }
    }
    
    /**
     * 문자열에 천 단위 구분 기호 추가
     * 
     * @param value 처리할 문자열
     * @return 쉼표가 추가된 문자열
     */
    private fun addCommas(value: String): String {
        val parts = value.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""
        
        val formatted = integerPart.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        
        return formatted + decimalPart
    }
    
    /**
     * 안전한 나눗셈 수행
     * 
     * @param dividend 피제수
     * @param divisor 제수
     * @param scale 결과의 소수점 자리수
     * @param roundingMode 반올림 모드
     * @return 나눗셈 결과 또는 null (0으로 나누는 경우)
     */
    fun safeDivide(
        dividend: BigDecimal?,
        divisor: BigDecimal?,
        scale: Int = 2,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): BigDecimal? {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return null
        }
        
        return dividend.divide(divisor, scale, roundingMode)
    }
}
