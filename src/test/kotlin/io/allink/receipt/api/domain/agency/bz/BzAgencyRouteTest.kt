package io.allink.receipt.api.domain.agency.bz

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.allink.receipt.api.domain.PagedResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.*
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer
import io.allink.receipt.api.config.plugin.UUIDSerializer
import io.allink.receipt.api.config.plugin.configureValidation
import io.allink.receipt.api.config.plugin.configureStatusPage

/**
 * @file BzAgencyRouteTest.kt
 * @brief bz agency 라우트 테스트 - 목록/상세/초기화/수정 응답 및 인증 검증
 */
class BzAgencyRouteTest {

  private val service: BzAgencyService = mockk(relaxed = true)

  private val issuer = "test-issuer"
  private val audience = "test-aud"
  private val secret = "test-secret"

  private fun Application.testModule() {
    // 예외를 4xx/5xx로 매핑
    configureStatusPage()
    // JSON 설정
    install(ContentNegotiation) { json(Json {
      serializersModule = SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
        contextual(UUID::class, UUIDSerializer)
      }
      ignoreUnknownKeys = true; encodeDefaults = true; isLenient = true; coerceInputValues = true }) }
    // 요청 검증
    configureValidation()
    // 인증
    install(Authentication) {
      jwt("auth-jwt") {
        verifier(
          JWT
            .require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
        )
        validate { JWTPrincipal(it.payload) }
      }
    }
    routing {
      authenticate("auth-jwt") {
        agencyRoutes(service)
      }
    }
  }

  private fun makeToken(claims: Map<String, String> = emptyMap()): String {
    val builder = JWT.create().withIssuer(issuer).withAudience(audience).withSubject("test-user")
    claims.forEach { (k, v) -> builder.withClaim(k, v) }
    return builder.sign(Algorithm.HMAC256(secret))
  }

  @Test
  fun `POST bz-agencies should return paged list`() = testApplication {
    application { testModule() }

    val item = SimpleBzAgencyModel(
      id = UUID.randomUUID(),
      agencyName = "대리점A",
      businessNo = "111-22-33333",
      status = AgencyStatus.ACTIVE,
      latestLoginAt = LocalDateTime.parse("2025-06-02T00:00:00")
    )
    coEvery { service.getAgencies(any()) } returns PagedResult(listOf(item), 1, 1, 1)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody("""{ "page": {"page":1, "pageSize":10} }""".trimIndent())
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
    kotlin.test.assertTrue(json.toString().contains("대리점A"))
  }

  @Test
  fun `POST bz-agencies should return 400 when service rejects filter`() = testApplication {
    application { testModule() }

    coEvery { service.getAgencies(any()) } throws BadRequestException("잘못된 요청")

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody("""{ "page": {"page":1, "pageSize":10} }""".trimIndent())
    }
    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }

  @Test
  fun `GET bz-agencies detail should return model`() = testApplication {
    application { testModule() }

    val id = UUID.randomUUID()
    val model = BzAgencyModel(id = id, agencyName = "대리점B", businessNo = "222", status = AgencyStatus.ACTIVE)
    coEvery { service.getAgency(id.toString()) } returns model

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.get("/bz-agencies/detail/$id") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }
    assertEquals(HttpStatusCode.OK, resp.status)
    kotlin.test.assertTrue(resp.bodyAsText().contains("대리점B"))
  }

  @Test
  fun `GET bz-agencies detail should return 400 on bad request`() = testApplication {
    application { testModule() }
    coEvery { service.getAgency("not-uuid") } throws BadRequestException("Invalid agency id")
    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.get("/bz-agencies/detail/not-uuid") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }
    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }

  @Test
  fun `GET bz-agencies detail should return 404 when not found`() = testApplication {
    application { testModule() }
    val id = UUID.randomUUID().toString()
    coEvery { service.getAgency(id) } throws NotFoundException("not found")
    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.get("/bz-agencies/detail/$id") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }
    assertEquals(HttpStatusCode.NotFound, resp.status)
  }

  @Test
  fun `POST bz-agencies init should return created uuid`() = testApplication {
    application { testModule() }

    val userUuid = UUID.randomUUID().toString()
    val created = UUID.randomUUID()
    coEvery { service.createdAgency(userUuid) } returns created

    val token = makeToken(mapOf("uUuid" to userUuid))
    val resp = client.post("/bz-agencies/init") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    kotlin.test.assertTrue(resp.bodyAsText().contains(created.toString()))
  }

  @Test
  fun `POST bz-agencies init without auth should return 401`() = testApplication {
    application { testModule() }
    val resp = client.post("/bz-agencies/init")
    assertEquals(HttpStatusCode.Unauthorized, resp.status)
  }

  @Test
  fun `POST bz-agencies modify should update and return model`() = testApplication {
    application { testModule() }

    val userUuid = UUID.randomUUID().toString()
    val agencyId = UUID.randomUUID()
    val requestBody = """
      {
        "id": "$agencyId",
        "agencyName": "수정대리점",
        "businessNo": "610-88-52114"
      }
    """.trimIndent()

    val updated = BzAgencyModel(
      id = agencyId,
      agencyName = "수정대리점",
      businessNo = "610-88-52114",
      status = AgencyStatus.ACTIVE
    )
    coEvery { service.updateAgency(any(), userUuid) } returns updated

    val token = makeToken(mapOf("uUuid" to userUuid))
    val resp = client.post("/bz-agencies/modify") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(requestBody)
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    kotlin.test.assertTrue(resp.bodyAsText().contains("수정대리점"))
  }

  @Test
  fun `POST bz-agencies modify should return 404 when not found`() = testApplication {
    application { testModule() }

    val userUuid = UUID.randomUUID().toString()
    val agencyId = UUID.randomUUID()
    coEvery { service.updateAgency(any(), userUuid) } throws NotFoundException("not found")

    val token = makeToken(mapOf("uUuid" to userUuid))
    val resp = client.post("/bz-agencies/modify") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody("""
        {
          "id": "$agencyId",
          "agencyName": "수정대리점",
          "businessNo": "610-88-52114"
        }
      """.trimIndent())
    }

    assertEquals(HttpStatusCode.NotFound, resp.status)
  }

  @Test
  fun `POST bz-agencies modify should return 400 when id missing`() = testApplication {
    application { testModule() }

    val userUuid = UUID.randomUUID().toString()
    coEvery { service.updateAgency(any(), userUuid) } throws BadRequestException("id required")

    val token = makeToken(mapOf("uUuid" to userUuid))
    val resp = client.post("/bz-agencies/modify") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody("""
        {
          "agencyName": "에이전시",
          "businessNo": "111-22-33333"
        }
      """.trimIndent())
    }

    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }

  @Test
  fun `POST bz-agencies should parse filter fields correctly`() = testApplication {
    application { testModule() }

    val capt = slot<BzAgencyFilter>()
    coEvery { service.getAgencies(capture(capt)) } returns PagedResult(emptyList(), 0, 0, 0)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "agencyName": "대",
          "businessNo": "123-45-67890",
          "status": "ACTIVE",
          "sort": [{"field":"agencyName","direction":"asc"}],
          "page": {"page":1, "pageSize":10}
        }
        """.trimIndent()
      )
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    // verify parsed values
    kotlin.test.assertEquals("대", capt.captured.agencyName)
    kotlin.test.assertEquals("123-45-67890", capt.captured.businessNo)
    kotlin.test.assertEquals(AgencyStatus.ACTIVE, capt.captured.status)
    kotlin.test.assertEquals("agencyName", capt.captured.sort?.first()?.field)
    kotlin.test.assertEquals("asc", capt.captured.sort?.first()?.direction)
  }

  @Test
  fun `POST bz-agencies invalid status should be coerced to null`() = testApplication {
    application { testModule() }

    val capt = slot<BzAgencyFilter>()
    coEvery { service.getAgencies(capture(capt)) } returns PagedResult(emptyList(), 0, 0, 0)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "status": "INVALID",
          "page": {"page":1, "pageSize":10}
        }
        """.trimIndent()
      )
    }
    // coerceInputValues=true 로 INVALID enum은 null 로 강제됨
    assertEquals(HttpStatusCode.OK, resp.status)
    kotlin.test.assertEquals(null, capt.captured.status)
  }

  @Test
  fun `POST bz-agencies invalid businessNo should return 400`() = testApplication {
    application { testModule() }

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))

    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "businessNo": "invalid-format",
          "page": {"page":1, "pageSize":10}
        }
        """.trimIndent()
      )
    }

    assertEquals(HttpStatusCode.BadRequest, resp.status)
  }

  @Test
  fun `POST bz-agencies should honor sort asc`() = testApplication {
    application { testModule() }

    val a = SimpleBzAgencyModel(UUID.randomUUID(), "가", "1", AgencyStatus.ACTIVE, null)
    val b = SimpleBzAgencyModel(UUID.randomUUID(), "나", "2", AgencyStatus.ACTIVE, null)
    coEvery { service.getAgencies(any()) } returns PagedResult(listOf(a, b), 1, 2, 1)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody("""{ "agencyName":"", "sort":[{"field":"agencyName","direction":"asc"}], "page": {"page":1, "pageSize":10} }""".trimIndent())
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    val body = resp.bodyAsText()
    kotlin.test.assertTrue(body.indexOf("가") < body.indexOf("나"))
  }

  @Test
  fun `POST bz-agencies should honor sort desc`() = testApplication {
    application { testModule() }

    val a = SimpleBzAgencyModel(UUID.randomUUID(), "가", "1", AgencyStatus.ACTIVE, null)
    val b = SimpleBzAgencyModel(UUID.randomUUID(), "나", "2", AgencyStatus.ACTIVE, null)
    coEvery { service.getAgencies(any()) } returns PagedResult(listOf(b, a), 1, 2, 1)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString()))
    val resp = client.post("/bz-agencies") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody("""{ "agencyName":"", "sort":[{"field":"agencyName","direction":"desc"}], "page": {"page":1, "pageSize":10} }""".trimIndent())
    }

    assertEquals(HttpStatusCode.OK, resp.status)
    val body = resp.bodyAsText()
    kotlin.test.assertTrue(body.indexOf("나") < body.indexOf("가"))
  }
}
