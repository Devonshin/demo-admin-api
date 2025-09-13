package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.Constant
import io.allink.receipt.api.util.AES256Util
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @file UserRepositoryMappingTest.kt
 * @brief UserRepository의 toModel 매핑 로직 단위 테스트(암복호화 포함)
 * @author Devonshin
 * @date 2025-09-12
 */
class UserRepositoryMappingTest {

  @Test
  fun `To model should decrypt sensitive fields`() {
    // given: ResultRow 를 모킹하여 컬럼 접근 시 값 반환
    val row = mockk<ResultRow>()

    val encryptedPhone = AES256Util.encrypt("01012345678", Constant.AES256_KEY)
    val encryptedEmail = AES256Util.encrypt("a@b.c", Constant.AES256_KEY)
    val encryptedNickname = AES256Util.encrypt("길동", Constant.AES256_KEY)

    every { row[UserTable.id] } returns "u-1"
    every { row[UserTable.name] } returns "홍길동"
    every { row[UserTable.status] } returns UserStatus.ACTIVE
    every { row[UserTable.phone] } returns encryptedPhone
    every { row[UserTable.gender] } returns "M"
    every { row[UserTable.ci] } returns null
    every { row[UserTable.birthday] } returns "1990-01-01"
    every { row[UserTable.localYn] } returns "Y"
    every { row[UserTable.email] } returns encryptedEmail
    every { row[UserTable.role] } returns UserRole.USER
    every { row[UserTable.joinSocialType] } returns "NAVER"
    every { row[UserTable.nickname] } returns encryptedNickname
    every { row[UserTable.mtchgId] } returns null
    every { row[UserTable.cpointRegType] } returns null
    every { row[UserTable.cpointRegDate] } returns null
    every { row[UserTable.regDate] } returns null
    every { row[UserTable.modDate] } returns null

    // when
    val model = UserRepository.Companion.toModel(row)

    // then
    assertEquals("u-1", model.id)
    assertEquals("홍길동", model.name)
    assertEquals(UserStatus.ACTIVE, model.status)
    assertEquals("01012345678", model.phone)
    assertEquals("M", model.gender)
    assertEquals("1990-01-01", model.birthday)
    assertEquals("Y", model.localYn)
    assertEquals("a@b.c", model.email)
    assertEquals(UserRole.USER, model.role)
    assertEquals("NAVER", model.joinSocialType)
    assertEquals("길동", model.nickname)
  }
}