package io.allink.receipt.api.domain.file

import io.allink.receipt.api.common.Constant
import io.allink.receipt.api.exception.InvalidFileUploadException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * @file FileRouteValidateTest.kt
 * @brief 파일 업로드 validate 함수 단위 테스트(메뉴/필드/확장자 검증)
 * @author Devonshin
 * @date 2025-09-12
 */
class FileRouteValidateTest {

  @Test
  fun `should throw when any parameter is null`() {
    assertThrows(InvalidFileUploadException::class.java) {
      validate(menu = null, field = null, fileName = null, extension = null, id = null, fileBytes = null)
    }
  }

  @Test
  fun `should throw when menu-field combination is not accepted`() {
    // stores 에 허용되지 않는 필드 사용
    assertThrows(InvalidFileUploadException::class.java) {
      validate(menu = "stores", field = "not-allowed", fileName = "a.png", extension = "png", id = "1", fileBytes = byteArrayOf(1))
    }
  }

  @Test
  fun `should throw when file type is not accepted`() {
    assertThrows(InvalidFileUploadException::class.java) {
      validate(menu = "stores", field = Constant.ACCEPT_FILE_FIELDS["stores"]!!.first(), fileName = "a.txt", extension = "txt", id = "1", fileBytes = byteArrayOf(1))
    }
  }

  @Test
  fun `should pass with accepted values`() {
    assertDoesNotThrow {
      validate(menu = "stores", field = Constant.ACCEPT_FILE_FIELDS["stores"]!!.first(), fileName = "a.${Constant.ACCEPT_FILE_TYPE.first()}", extension = Constant.ACCEPT_FILE_TYPE.first(), id = "1", fileBytes = byteArrayOf(1))
    }
  }
}