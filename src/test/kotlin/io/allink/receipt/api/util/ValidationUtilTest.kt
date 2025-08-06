package io.allink.receipt.api.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Test class for ValidationUtil
 * Tests business number validation logic
 */
class ValidationUtilTest {

  @Test
  @DisplayName("유효한 사업자등록번호 형식과 체크디지트가 올바른 경우")
  fun testValidBusinessNumbers() {
    // 실제 유효한 사업자등록번호 예시들
    val validBusinessNumbers = listOf(
      "4578701531", // 요청받은 유효한 번호
      "972-67-51148",
      "610-88-52114",
      "504-66-01996",
      "644-16-68984",
      "575-09-98369",
      "312-96-76503",
      "494-23-01538",
      "598-20-37486",
      "647-68-93894",
      "340-45-48785",
      "773-32-16498",
      "427-32-12978",
      "689-53-59028",
      "205-26-63406",
      "934-94-95214",
      "106-86-29594"
    )

    validBusinessNumbers.forEach { businessNo ->
      assertTrue(
        isValidBusinessNo(businessNo),
        "Valid business number should return true: $businessNo"
      )
    }
  }

  @Test
  @DisplayName("잘못된 형식의 사업자등록번호")
  fun testInvalidFormat() {
    val invalidFormats = listOf(
      "12345678901",     // 하이픈 없음
      "123-456-78901",   // 잘못된 패턴
      "12-345-67890",    // 잘못된 패턴
      "123-45-6789",     // 짧은 길이
      "123-45-678901",   // 긴 길이
      "abc-de-fghij",    // 문자 포함
      "123-4a-67890",    // 중간에 문자
      "",                // 빈 문자열
      "   ",             // 공백만
      "123--67890"       // 하이픈 중복
    )

    invalidFormats.forEach { businessNo ->
      assertFalse(
        isValidBusinessNo(businessNo),
        "Invalid format should return false: $businessNo"
      )
    }
  }

  @Test
  @DisplayName("올바른 형식이지만 체크디지트가 틀린 경우")
  fun testValidFormatButInvalidCheckDigit() {
    val invalidCheckDigitNumbers = listOf(
      "101-81-12345", // 마지막 자리가 틀림
      "102-19-75326",
      "104-86-78900",
      "105-87-00123", // 올바른 번호에서 마지막 자리만 변경
    )

    invalidCheckDigitNumbers.forEach { businessNo ->
      assertFalse(
        isValidBusinessNo(businessNo),
        "Invalid check digit should return false: $businessNo"
      )
    }
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "000-00-00000",
      "999-99-99999",
      "111-11-11111"
    ]
  )
  @DisplayName("특수한 패턴의 사업자등록번호 테스트")
  fun testSpecialPatterns(businessNo: String) {
    // 이런 패턴들은 형식은 맞지만 체크디지트 검증에 따라 결과가 달라짐
    val result = isValidBusinessNo(businessNo)
    // 결과가 true든 false든 예외가 발생하지 않아야 함
    assertNotNull(result)
  }

  @Test
  @DisplayName("경계값 테스트")
  fun testBoundaryValues() {
    // 최소값과 최대값 패턴
    assertFalse(isValidBusinessNo("000-00-00000"))

    // 각 자리수의 경계값들
    val boundaryTests = listOf(
      "100-00-00000",
      "999-99-99999",
      "000-01-00000",
      "000-99-00000",
      "000-00-00001",
      "000-00-99999"
    )

    boundaryTests.forEach { businessNo ->
      val result = isValidBusinessNo(businessNo)
      // 경계값들이 예외를 발생시키지 않는지 확인
      assertNotNull(result)
    }
  }

  @Test
  @DisplayName("null 및 예외 상황 테스트")
  fun testExceptionCases() {
    // 빈 문자열
    assertFalse(isValidBusinessNo(""))

    // 공백 문자열
    assertFalse(isValidBusinessNo("   "))

    // 특수문자 포함
    assertFalse(isValidBusinessNo("123-45-6789@"))
    assertFalse(isValidBusinessNo("123-45-6789!"))
  }
}