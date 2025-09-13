package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.Response
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer
import io.allink.receipt.api.config.plugin.UUIDSerializer

/**
 * @file IssueReceiptRouteTest.kt
 * @brief receipt 라우트 테스트 - DTO 응답 구조 검증 및 서비스 목킹
 * @author Devonshin
 * @date 2025-09-12
 */
class IssueReceiptRouteTest {

  private val service: IssueReceiptService = mockk(relaxed = true)

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
    routing {
      issueReceiptRoutes(service)
    }
  }

  @Test
  fun `POST receipts should return paged result`() = testApplication {
    application { testModule() }

    val sample = SimpleIssueReceiptModel(
      id = "R-1",
      store = SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      tagId = "T-1",
      issueDate = LocalDateTime.parse("2025-06-02T00:00:00"),
      user = SimpleUserModel(id = "U-1", name = "홍길동"),
      receiptType = "PAY",
      receiptAmount = 1000,
      originIssueId = null
    )
    val page = PagedResult(items = listOf(sample), totalCount = 1, currentPage = 1, totalPages = 1)

    coEvery { service.findAllReceipt(any()) } returns page

    val resp = client.post("/receipts") {
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
    // Response wrapper
    val data = json["data"] as JsonElement
    val dataObj: JsonObject = data.jsonObject
    assertEquals(1, dataObj["totalCount"].toString().toInt())
  }

  @Test
  fun `GET receipt detail should return model or null`() = testApplication {
    application { testModule() }

    val model = IssueReceiptModel(
      id = "R-1",
      store = SimpleStoreModel(id = "S-1", storeName = "매장", franchiseCode = null, businessNo = null, ceoName = null),
      tag = SimpleMerchantTagReceiptModel(id = "T-1", deviceId = "D-1"),
      issueDate = LocalDateTime.parse("2025-06-02T00:00:00"),
      user = SimpleUserModel(id = "U-1", name = "홍길동"),
      receiptType = "PAY",
      receiptAmount = 1000,
      originIssueId = null,
      userPointReview = null,
      edoc = null,
      advertisement = null
    )

    coEvery { service.findReceipt("U-1", "R-1") } returns model

    val resp = client.get("/receipts/detail/U-1/R-1")

    assertEquals(HttpStatusCode.OK, resp.status)
    val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
    val data = json["data"]!!.jsonObject
    assertEquals("R-1", data["id"]!!.toString().trim('"'))
  }

  @Test
  fun `POST receipts should return 400 when period is missing`() = testApplication {
    application { testModule() }

    val resp = client.post("/receipts") {
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "page": {"page":1, "pageSize":10}
        }
        """.trimIndent()
      )
    }

    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }

  @Test
  fun `POST receipts should return 400 when period dates are malformed`() = testApplication {
    application { testModule() }

    val resp = client.post("/receipts") {
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "period": {"from":"not-a-date", "to":"also-not-a-date"},
          "page": {"page":1, "pageSize":10}
        }
        """.trimIndent()
      )
    }

    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }
}
