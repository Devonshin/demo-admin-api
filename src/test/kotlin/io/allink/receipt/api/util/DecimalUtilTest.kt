/**
 * @file DecimalUtilTest.kt
 * @brief DecimalUtil 클래스의 단위 테스트
 * @author Devonshin
 * @date 2025-08-21
 */
package io.allink.receipt.api.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * DecimalUtil 클래스의 단위 테스트
 * 
 * BigDecimal 관련 유틸리티 메서드들의 정확성을 검증합니다.
 */
class DecimalUtilTest {

    @Test
    @DisplayName("toBigDecimal - 유효한 문자열을 BigDecimal로 변환")
    fun testToBigDecimalWithValidString() {
        // 정수
        assertEquals(BigDecimal("123"), DecimalUtil.toBigDecimal("123"))
        
        // 소수
        assertEquals(BigDecimal("123.45"), DecimalUtil.toBigDecimal("123.45"))
        
        // 음수
        assertEquals(BigDecimal("-123.45"), DecimalUtil.toBigDecimal("-123.45"))
        
        // 쉼표 포함
        assertEquals(BigDecimal("1234567.89"), DecimalUtil.toBigDecimal("1,234,567.89"))
        
        // 공백 포함
        assertEquals(BigDecimal("123.45"), DecimalUtil.toBigDecimal(" 123.45 "))
    }
    
    @Test
    @DisplayName("toBigDecimal - 무효한 문자열 처리")
    fun testToBigDecimalWithInvalidString() {
        // null
        assertEquals(BigDecimal.ZERO, DecimalUtil.toBigDecimal(null))
        
        // 빈 문자열
        assertEquals(BigDecimal.ZERO, DecimalUtil.toBigDecimal(""))
        
        // 공백만
        assertEquals(BigDecimal.ZERO, DecimalUtil.toBigDecimal("   "))
        
        // 숫자가 아닌 문자열
        assertEquals(BigDecimal.ZERO, DecimalUtil.toBigDecimal("abc"))
        
        // 특수문자
        assertEquals(BigDecimal.ZERO, DecimalUtil.toBigDecimal("@#$"))
        
        // 커스텀 기본값
        assertEquals(BigDecimal.TEN, DecimalUtil.toBigDecimal("invalid", BigDecimal.TEN))
    }
    
    @Test
    @DisplayName("round - 다양한 반올림 테스트")
    fun testRound() {
        val value = BigDecimal("123.456789")
        
        // 기본 반올림 (소수점 2자리, HALF_UP)
        assertEquals(BigDecimal("123.46"), DecimalUtil.round(value))
        
        // 소수점 0자리
        assertEquals(BigDecimal("123"), DecimalUtil.round(value, 0))
        
        // 소수점 4자리
        assertEquals(BigDecimal("123.4568"), DecimalUtil.round(value, 4))
        
        // HALF_DOWN 모드
        val value2 = BigDecimal("123.455")
        assertEquals(BigDecimal("123.45"), DecimalUtil.round(value2, 2, RoundingMode.HALF_DOWN))
        
        // CEILING 모드
        assertEquals(BigDecimal("123.46"), DecimalUtil.round(value2, 2, RoundingMode.CEILING))
        
        // FLOOR 모드
        assertEquals(BigDecimal("123.45"), DecimalUtil.round(value2, 2, RoundingMode.FLOOR))
    }
    
    @Test
    @DisplayName("add - 두 BigDecimal 더하기")
    fun testAdd() {
        // 일반적인 더하기
        assertEquals(
            BigDecimal("30.50"), 
            DecimalUtil.add(BigDecimal("10.25"), BigDecimal("20.25"))
        )
        
        // null 처리
        assertEquals(BigDecimal("10.25"), DecimalUtil.add(BigDecimal("10.25"), null))
        assertEquals(BigDecimal("20.25"), DecimalUtil.add(null, BigDecimal("20.25")))
        assertEquals(BigDecimal.ZERO, DecimalUtil.add(null, null))
        
        // 음수 포함
        assertEquals(
            BigDecimal("-5.00"), 
            DecimalUtil.add(BigDecimal("10.00"), BigDecimal("-15.00"))
        )
    }
    
    @Test
    @DisplayName("sum - 여러 BigDecimal 합계")
    fun testSum() {
        // 여러 값의 합계
        assertEquals(
            BigDecimal("100.00"),
            DecimalUtil.sum(
                BigDecimal("10.00"),
                BigDecimal("20.00"),
                BigDecimal("30.00"),
                BigDecimal("40.00")
            )
        )
        
        // null 포함
        assertEquals(
            BigDecimal("60.00"),
            DecimalUtil.sum(
                BigDecimal("10.00"),
                null,
                BigDecimal("20.00"),
                null,
                BigDecimal("30.00")
            )
        )
        
        // 모두 null
        assertEquals(BigDecimal.ZERO, DecimalUtil.sum(null, null, null))
        
        // 빈 배열
        assertEquals(BigDecimal.ZERO, DecimalUtil.sum())
    }
    
    @ParameterizedTest
    @CsvSource(
        "100.00, 10, 10.00",
        "100.00, 15, 15.00",
        "100.00, 25.5, 25.50",
        "1000.00, 5, 50.00",
        "50.00, 100, 50.00"
    )
    @DisplayName("calculatePercentage - 백분율 계산")
    fun testCalculatePercentage(value: String, percentage: String, expected: String) {
        assertEquals(
            BigDecimal(expected),
            DecimalUtil.calculatePercentage(BigDecimal(value), BigDecimal(percentage))
        )
    }
    
    @Test
    @DisplayName("format - 숫자 포맷팅")
    fun testFormat() {
        // 기본 포맷팅 (소수점 2자리, 쉼표 포함)
        assertEquals("1,234.57", DecimalUtil.format(BigDecimal("1234.567")))
        
        // 소수점 0자리
        assertEquals("1,235", DecimalUtil.format(BigDecimal("1234.567"), 0))
        
        // 소수점 4자리
        assertEquals("1,234.5670", DecimalUtil.format(BigDecimal("1234.567"), 4))
        
        // 쉼표 제외
        assertEquals("1234.57", DecimalUtil.format(BigDecimal("1234.567"), 2, false))
        
        // 큰 숫자
        assertEquals(
            "1,234,567,890.12", 
            DecimalUtil.format(BigDecimal("1234567890.123"))
        )
        
        // 작은 숫자
        assertEquals("0.12", DecimalUtil.format(BigDecimal("0.123")))
        
        // 음수
        assertEquals("-1,234.57", DecimalUtil.format(BigDecimal("-1234.567")))
    }
    
    @Test
    @DisplayName("safeDivide - 안전한 나눗셈")
    fun testSafeDivide() {
        // 일반적인 나눗셈
        assertEquals(
            BigDecimal("25.00"),
            DecimalUtil.safeDivide(BigDecimal("100"), BigDecimal("4"))
        )
        
        // 무한소수 처리
        assertEquals(
            BigDecimal("33.33"),
            DecimalUtil.safeDivide(BigDecimal("100"), BigDecimal("3"))
        )
        
        // 더 많은 소수점 자리
        assertEquals(
            BigDecimal("33.3333"),
            DecimalUtil.safeDivide(BigDecimal("100"), BigDecimal("3"), 4)
        )
        
        // 0으로 나누기
        assertNull(DecimalUtil.safeDivide(BigDecimal("100"), BigDecimal.ZERO))
        
        // null 처리
        assertNull(DecimalUtil.safeDivide(null, BigDecimal("10")))
        assertNull(DecimalUtil.safeDivide(BigDecimal("100"), null))
        assertNull(DecimalUtil.safeDivide(null, null))
        
        // 다른 반올림 모드
        assertEquals(
            BigDecimal("33.34"),
            DecimalUtil.safeDivide(
                BigDecimal("100"), 
                BigDecimal("3"), 
                2, 
                RoundingMode.CEILING
            )
        )
    }
    
    @Test
    @DisplayName("복합 시나리오 - 실제 사용 예시")
    fun testRealWorldScenarios() {
        // 시나리오 1: 상품 가격 계산 (부가세 포함)
        val productPrice = DecimalUtil.toBigDecimal("50000")
        val vatRate = BigDecimal("10") // 10%
        val vat = DecimalUtil.calculatePercentage(productPrice, vatRate)
        val totalPrice = DecimalUtil.add(productPrice, vat)
        
        assertEquals(BigDecimal("5000.00"), vat)
        assertEquals(BigDecimal("55000.00"), totalPrice)
        assertEquals("55,000.00", DecimalUtil.format(totalPrice))
        
        // 시나리오 2: 평균 계산
        val sales = arrayOf(
            BigDecimal("1000"),
            BigDecimal("1500"),
            BigDecimal("2000"),
            BigDecimal("1200")
        )
        val totalSales = DecimalUtil.sum(*sales)
        val averageSales = DecimalUtil.safeDivide(totalSales, BigDecimal(sales.size))
        
        assertEquals(BigDecimal("5700"), totalSales)
        assertEquals(BigDecimal("1425.00"), averageSales)
        
        // 시나리오 3: 할인 적용
        val originalPrice = BigDecimal("89900")
        val discountRate = BigDecimal("15") // 15% 할인
        val discount = DecimalUtil.calculatePercentage(originalPrice, discountRate)
        val finalPrice = DecimalUtil.add(originalPrice, discount.negate())
        
        assertEquals(BigDecimal("13485.00"), discount)
        assertEquals(BigDecimal("76415.00"), finalPrice)
        assertEquals("76,415.00", DecimalUtil.format(finalPrice))
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "0",
        "0.0",
        "0.00",
        "-0",
        "-0.0"
    ])
    @DisplayName("경계값 테스트 - 0 처리")
    fun testZeroHandling(value: String) {
        val decimal = DecimalUtil.toBigDecimal(value)
        
        // 0으로 더하기 - 결과의 값만 비교 (스케일 무시)
        val result = DecimalUtil.add(BigDecimal("10"), decimal)
        assertTrue(result.compareTo(BigDecimal("10")) == 0, 
            "Expected value to be 10, but was $result")
        
        // 0으로 곱하기 (백분율)
        assertEquals(
            BigDecimal("0.00"), 
            DecimalUtil.calculatePercentage(BigDecimal("100"), BigDecimal.ZERO)
        )
        
        // 0 포맷팅
        assertEquals("0.00", DecimalUtil.format(decimal))
    }
    
    @Test
    @DisplayName("큰 숫자 처리")
    fun testLargeNumbers() {
        val largeNumber = BigDecimal("999999999999999999.99")
        val formatted = DecimalUtil.format(largeNumber)
        
        assertEquals("999,999,999,999,999,999.99", formatted)
        
        // 큰 숫자끼리 더하기
        val sum = DecimalUtil.add(largeNumber, largeNumber)
        assertEquals(BigDecimal("1999999999999999999.98"), sum)
    }
    
    @Test
    @DisplayName("작은 소수 처리")
    fun testSmallDecimals() {
        val smallNumber = BigDecimal("0.000001")
        
        // 많은 소수점 자리수로 포맷팅
        assertEquals("0.000001", DecimalUtil.format(smallNumber, 6))
        
        // 적은 소수점 자리수로 반올림
        assertEquals("0.00", DecimalUtil.format(smallNumber, 2))
        
        // 백분율 계산
        val percentage = DecimalUtil.calculatePercentage(
            BigDecimal("100"), 
            BigDecimal("0.01")
        )
        assertEquals(BigDecimal("0.01"), percentage)
    }
}
