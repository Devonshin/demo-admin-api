package io.allink.receipt.api.domain.koces

import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer
import io.allink.receipt.api.config.plugin.UUIDSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class KocesServiceImplBasicTest {

  private fun testConfig(): ApplicationConfig = MapApplicationConfig().apply {
    put("receiptGateKocesPay.baseUrl", "https://example.com")
    put("receiptGateKocesPay.paymentPath", "/pay")
    put("receiptGateKocesPay.cancelPath", "/cancel")
    put("receiptGateKocesPay.timeoutSeconds", "5")
    put("receiptGateKocesPay.maxRetries", "0")
    put("receiptGateKocesPay.token", "test-token")
  }

@Test
  fun `requestPayment should return OK on success response`() = runTestBlocking {
    val engine = MockEngine { req ->
      assertEquals("/pay", req.url.encodedPath)
      val body = """
        {"resultCode":"OK","resultData":{"PAY":{"responseSeq":123,"message":"success"}},"resultMessage":"0000"}
      """.trimIndent()
      respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val client = HttpClient(engine) {
      install(ContentNegotiation) {
        json(Json {
          serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
            contextual(LocalDateTime::class, LocalDateTimeSerializer)
          }
          ignoreUnknownKeys = true
          encodeDefaults = true
        })
      }
      install(HttpTimeout) { requestTimeoutMillis = 5_000 }
    }
    val service = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val result = service.requestPayment(7001L)
    assertEquals("OK", result.resultCode)
    assertEquals("0000", result.resultMessage)
  }

  @Test
  fun `cancelPayment should return OK on success response`() = runTestBlocking {
    val engine = MockEngine { req ->
      assertEquals("/cancel", req.url.encodedPath)
      val body = """
        {"resultCode":"OK","resultMessage":"0000","resultData":{"CANCEL":{"responseSeq":456,"message":"cancelled"}}}
      """.trimIndent()
      respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val client = HttpClient(engine) {
      install(ContentNegotiation) {
        json(Json {
          serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
            contextual(LocalDateTime::class, LocalDateTimeSerializer)
          }
          ignoreUnknownKeys = true
          encodeDefaults = true
        })
      }
      install(HttpTimeout) { requestTimeoutMillis = 5_000 }
    }
    val service = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val result = service.cancelPayment(20250107001, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
    assertEquals("OK", result.resultCode)
    assertEquals("0000", result.resultMessage)
  }


  @Test
  fun `requestPayment should return NOTOK with ERROR on generic exception`() = runTestBlocking {
    val engine = MockEngine { _ -> throw RuntimeException("boom") }
    val client = HttpClient(engine) {
      install(ContentNegotiation) {
        json(Json {
          serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
            contextual(LocalDateTime::class, LocalDateTimeSerializer)
          }
          ignoreUnknownKeys = true
          encodeDefaults = true
        })
      }
    }
    val service = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val result = service.requestPayment(1L)
    assertEquals("NOTOK", result.resultCode)
    assertEquals("ERROR", result.errorCode)
  }
}

// Minimal runTest helper to avoid bringing kotlinx-coroutines-test explicitly if not present
private fun runTestBlocking(block: suspend () -> Unit) {
  kotlinx.coroutines.runBlocking { block() }
}
