package io.allink.receipt.api.domain.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * @file UserModelTest.kt
 * @brief user 도메인의 Model/Enum 로직 단위 테스트
 * @author Devonshin
 * @date 2025-09-12
 */
class UserModelTest {

  @Test
  fun `Should return enum when from is valid`() {
    assertEquals(UserStatus.ACTIVE, UserStatus.from("active"))
    assertEquals(UserStatus.NORMAL, UserStatus.from("NORMAL"))
    assertEquals(UserStatus.INACTIVE, UserStatus.from("InActive"))
  }

  @Test
  fun `Should throw when from is invalid`() {
    assertThrows(IllegalStateException::class.java) {
      UserStatus.from("UNKNOWN")
    }
  }

  @Test
  fun `Age value object should hold range`() {
    val age = Age(from = "1980", to = "1990")
    assertEquals("1980", age.from)
    assertEquals("1990", age.to)
  }

}