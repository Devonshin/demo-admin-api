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
class KocesServiceImplErrorTest {

  private fun testConfig(): ApplicationConfig = MapApplicationConfig().apply {
    put("receiptGateKocesPay.baseUrl", "https://example.com")
    put("receiptGateKocesPay.paymentPath", "/pay")
    put("receiptGateKocesPay.cancelPath", "/cancel")
    put("receiptGateKocesPay.timeoutSeconds", "5")
    put("receiptGateKocesPay.maxRetries", "0")
    put("receiptGateKocesPay.token", "test-token")
  }

  private fun buildClient(engine: MockEngine): HttpClient = HttpClient(engine) {
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

  @Test
  fun `requestPayment returns NOTOK with resultMessage on 400 body NOTOK`() = runTestBlocking {
    val engine = MockEngine { req ->
      assertEquals("/pay", req.url.encodedPath)
      val body = "{" +
        "\"resultCode\":\"NOTOK\",\n" +
        "\"resultMessage\":\"1234\"" +
        "}"
      respond(
        content = body,
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val client = buildClient(engine)
    val svc = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val resp = svc.requestPayment(1L)
    assertEquals("NOTOK", resp.resultCode)
    assertEquals("1234", resp.resultMessage)
  }

  @Test
  fun `cancelPayment returns NOTOK on 500 body NOTOK`() = runTestBlocking {
    val engine = MockEngine { req ->
      assertEquals("/cancel", req.url.encodedPath)
      val body = "{" +
        "\"resultCode\":\"NOTOK\",\n" +
        "\"resultMessage\":\"E500\"" +
        "}"
      respond(
        content = body,
        status = HttpStatusCode.InternalServerError,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val client = buildClient(engine)
    val svc = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val resp = svc.cancelPayment(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
    assertEquals("NOTOK", resp.resultCode)
    assertEquals("E500", resp.resultMessage)
  }

  @Test
  fun `requestPayment returns NOTOK ERROR on malformed JSON`() = runTestBlocking {
    val engine = MockEngine { _ ->
      respond(
        content = "{", // malformed
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val client = buildClient(engine)
    val svc = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val resp = svc.requestPayment(2L)
    assertEquals("NOTOK", resp.resultCode)
    assertEquals("ERROR", resp.errorCode)
  }

  @Test
  fun `cancelPayment returns NOTOK ERROR when resultCode missing`() = runTestBlocking {
    val engine = MockEngine { _ ->
      val body = "{" +
        "\"resultMessage\":\"oops\"" +
        "}"
      respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val client = buildClient(engine)
    val svc = KocesServiceImpl(client, KocesGatewayConfig(testConfig()))

    val resp = svc.cancelPayment(3L, UUID.randomUUID())
    assertEquals("NOTOK", resp.resultCode)
    assertEquals("ERROR", resp.errorCode)
  }
}

private fun runTestBlocking(block: suspend () -> Unit) {
  kotlinx.coroutines.runBlocking { block() }
}