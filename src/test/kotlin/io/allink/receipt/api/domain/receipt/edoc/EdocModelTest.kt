package io.allink.receipt.api.domain.receipt.edoc

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlinx.serialization.modules.SerializersModule
import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer

/**
 * @file EdocModelTest.kt
 * @brief edoc 패키지의 직렬화 스모크 테스트(모델 생성/직렬화로 라인 커버리지 확보)
 * @author Devonshin
 * @date 2025-09-12
 */
class EdocModelTest {
  @Test
  fun `kakao and naver edoc models should serialize`() {
    val kakao = KakaoEdocModel(
      id = "partner-1",
      receiptUuid = "r-1",
      envelopId = "e-1",
      responseCode = "0000",
      regDate = "2025-03-05T13:08:12",
      userId = "u-1"
    )
    val json1 = Json.encodeToString(kakao)
    assertTrue(json1.contains("\"envelopId\":\"e-1\""))

    val naver = NaverEdocModel(
      id = "partner-2",
      receiptUuid = "r-2",
      envelopId = "n-1",
      responseCode = "0000",
      regDate = LocalDateTime.parse("2025-03-05T13:08:12"),
      userId = "u-2"
    )
    val jsonConfigured = Json {
      serializersModule = SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
      }
    }
    val json2 = jsonConfigured.encodeToString(naver)
    // 클래스 이름이 포함되지는 않으므로 필드 존재로 검증
    assertTrue(json2.contains("\"envelopId\":\"n-1\""))
  }

  @Test
  fun `edoc model base should serialize`() {
    val edoc = EdocModel(
      sender = "kakao",
      id = "partner-3",
      receiptUuid = "r-3",
      envelopId = "e-3",
      responseCode = "0000",
      regDate = "2025-06-01T00:00:00",
      userId = "u-3"
    )
    val jsonConfigured = Json {
      serializersModule = SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
      }
    }
    val json = jsonConfigured.encodeToString(edoc)
    assertTrue(json.contains("\"sender\":\"kakao\""))
  }
}