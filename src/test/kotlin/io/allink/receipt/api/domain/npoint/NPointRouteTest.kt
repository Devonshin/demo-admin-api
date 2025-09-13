package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.domain.receipt.SimpleMerchantTagReceiptModel
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer
import io.allink.receipt.api.config.plugin.UUIDSerializer

/**
 * @file NPointRouteTest.kt
 * @brief npoint 라우트 테스트 - GET with body 케이스 검증 및 DTO 응답 구조 검증
 */
class NPointRouteTest {

  private val service: NPointService = mockk(relaxed = true)

  private fun Application.testModule() {
    install(ContentNegotiation) {
      json(Json {
        serializersModule = SerializersModule {
          contextual(LocalDateTime::class, LocalDateTimeSerializer)
          contextual(UUID::class, UUIDSerializer)
        }
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
      })
    }
    routing { pointRoutes(service) }
  }

  @Test
  fun `GET points should return paged result`() = testApplication {
    application { testModule() }

    val sample = NPointPayModel(
      id = 1L,
      point = 100,
      status = "지급완료",
      user = NPointUserModel(id = "U-1", name = "홍길동", phone = "010", gender = "M", birthday = "1990", nickname = "길동"),
      store = SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      provideCase = "이벤트",
      pointTrNo = "TR-1",
      pointPayNo = "PAY-1",
      regDate = LocalDateTime.parse("2025-06-02T00:00:00")
    )
    coEvery { service.getAllNPointPay(any()) } returns PagedResult(listOf(sample), 1, 1, 1)

    val resp = client.get("/points") {
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "period": {"from":"2025-06-01T00:00:00", "to":"2025-06-30T23:59:59"},
          "page": {"page":1, "pageSize":10}
        }
        """.trimIndent()
      )
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
    val data = json["data"]!!.jsonObject
    assertEquals(1, data["totalCount"]!!.toString().toInt())
  }

  @Test
  fun `GET points should return 400 on bad request`() = testApplication {
    application { testModule() }

    coEvery { service.getAllNPointPay(any()) } throws io.ktor.server.plugins.BadRequestException("bad filter")

    val resp = client.get("/points") {
      contentType(ContentType.Application.Json)
      setBody("{" +
        "\"period\": {\"from\": \"2025-06-30T00:00:00\", \"to\": \"2025-06-01T00:00:00\"}," +
        "\"page\": {\"page\":1, \"pageSize\":10}" +
        "}")
    }
    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }
}
