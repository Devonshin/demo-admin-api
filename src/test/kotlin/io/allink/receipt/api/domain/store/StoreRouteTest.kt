package io.allink.receipt.api.domain.store

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.allink.receipt.api.config.plugin.LocalDateTimeSerializer
import io.allink.receipt.api.config.plugin.UUIDSerializer
import io.allink.receipt.api.config.plugin.configureStatusPage
import io.allink.receipt.api.config.plugin.configureValidation
import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.domain.Sorter
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class StoreRouteTest {

  private val storeService: StoreService = mockk(relaxed = true)
  private val billingService: StoreBillingService = mockk(relaxed = true)

  private val issuer = "test-issuer"
  private val audience = "test-aud"
  private val secret = "test-secret"

  private fun Application.testModule() {
    // 예외 매핑
    configureStatusPage()
    // JSON 직렬화
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
    // 요청 검증
    configureValidation()
    // JWT 인증
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
        storeRoutes(storeService, billingService)
      }
    }
  }

  private fun makeToken(claims: Map<String, String> = emptyMap()): String {
    val builder = JWT.create().withIssuer(issuer).withAudience(audience).withSubject("test-user")
    claims.forEach { (k, v) -> builder.withClaim(k, v) }
    return builder.sign(Algorithm.HMAC256(secret))
  }

  @Test
  fun `POST stores should call agency or global listing based on role`() = testApplication {
    application { testModule() }

    val item = StoreSearchModel(id = "S-1", storeName = "상점A")
    coEvery { storeService.findAllAgencyStore(any(), any()) } returns PagedResult(listOf(item), 1, 1, 1)
    coEvery { storeService.findAllStore(any()) } returns PagedResult(listOf(item), 1, 1, 1)

    val filterJson = """
      {
        "period": {"from":"2025-06-01T00:00:00", "to":"2025-06-30T23:59:59"},
        "sort": [{"field":"name", "direction":"asc"}],
        "page": {"page":1, "pageSize":10}
      }
    """.trimIndent()

    // 대리점 마스터 경로
    val tokenAgency = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "BZ_AGENCY_MASTER", "agencyId" to UUID.randomUUID().toString()))
    val r1 = client.post("/stores") {
      header(HttpHeaders.Authorization, "Bearer $tokenAgency")
      contentType(ContentType.Application.Json)
      setBody(filterJson)
    }
    assertEquals(HttpStatusCode.OK, r1.status)

    // 글로벌 경로
    val tokenMaster = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r2 = client.post("/stores") {
      header(HttpHeaders.Authorization, "Bearer $tokenMaster")
      contentType(ContentType.Application.Json)
      setBody(filterJson)
    }
    assertEquals(HttpStatusCode.OK, r2.status)

    // 호출 검증
    coVerify { storeService.findAllAgencyStore(any(), any()) }
    coVerify { storeService.findAllStore(any()) }
  }

  @Test
  fun `GET store detail should switch by role`() = testApplication {
    application { testModule() }

    val model = StoreModel(id = "S-9", storeName = "상세상점")
    coEvery { storeService.findStore("S-9", any()) } returns model
    coEvery { storeService.findStore("S-9") } returns model

    val tokenAgency = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "BZ_AGENCY_MASTER", "agencyId" to UUID.randomUUID().toString()))
    val r1 = client.get("/stores/detail/S-9") { header(HttpHeaders.Authorization, "Bearer $tokenAgency") }
    assertEquals(HttpStatusCode.OK, r1.status)

    val tokenMaster = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r2 = client.get("/stores/detail/S-9") { header(HttpHeaders.Authorization, "Bearer $tokenMaster") }
    assertEquals(HttpStatusCode.OK, r2.status)

    // 호출 검증
    coVerify { storeService.findStore("S-9", any()) }
    coVerify { storeService.findStore("S-9") }
  }

  @Test
  fun `POST stores search should return paged stores`() = testApplication {
    application { testModule() }

    val item = StoreSearchModel(id = "S-2", storeName = "상점B")
    coEvery { storeService.findSearchStores(any()) } returns PagedResult(listOf(item), 1, 1, 1)

    val r = client.post("/stores/search") {
      val body = """
        {
          "name": "상점",
          "sort": [{"field":"name", "direction":"asc"}],
          "page": {"page":1, "pageSize":10}
        }
      """.trimIndent()
      val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(body)
    }
    assertEquals(HttpStatusCode.OK, r.status)
    val json = Json.parseToJsonElement(r.bodyAsText()).jsonObject
    kotlin.test.assertTrue(json.toString().contains("상점B"))
  }

  @Test
  fun `GET billing tokens by businessNo should return list`() = testApplication {
    application { testModule() }

    coEvery { storeService.findAllBillingToken("111-11-11111") } returns listOf(
      StoreBillingTokenModel(id = UUID.randomUUID(), businessNo = "111-11-11111", token = "TK", tokenInfo = null, status = io.allink.receipt.api.common.StatusCode.ACTIVE, regDate = LocalDateTime.parse("2025-06-02T00:00:00"), regBy = UUID.randomUUID())
    )

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r = client.get("/stores/billing-tokens/111-11-11111") { header(HttpHeaders.Authorization, "Bearer $token") }
    assertEquals(HttpStatusCode.OK, r.status)
    kotlin.test.assertTrue(r.bodyAsText().contains("\"token\":"))
  }

  @Test
  fun `POST store regist should trigger billing payment when storeBilling is present`() = testApplication {
    application { testModule() }

    val newStoreId = "S-1000"
    coEvery { storeService.registStore(any(), any()) } returns newStoreId
    val billing = StoreBillingModel(id = 1L, storeUid = newStoreId, storeServiceSeq = 10, tokenUuid = UUID.randomUUID(), billingAmount = 1000, status = io.allink.receipt.api.common.BillingStatusCode.STANDBY, bankCode = null, bankAccountNo = null, bankAccountName = null, regBy = UUID.randomUUID(), regDate = LocalDateTime.parse("2025-06-02T00:00:00"))
    coEvery { storeService.findStore(newStoreId) } returns StoreModel(id = newStoreId, storeName = "신규", storeBilling = billing)
    coEvery { billingService.paymentStoreBilling(any()) } returns billing.copy(status = io.allink.receipt.api.common.BillingStatusCode.COMPLETE)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r = client.post("/stores/regist") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "storeName": "신규",
          "businessNo": "111-11-11111"
        }
        """.trimIndent()
      )
    }
    assertEquals(HttpStatusCode.OK, r.status)
    coVerify { billingService.paymentStoreBilling(any()) }
  }

  @Test
  fun `POST store regist should not call billing payment when storeBilling is null`() = testApplication {
    application { testModule() }

    val newStoreId = "S-1001"
    coEvery { storeService.registStore(any(), any()) } returns newStoreId
    // 등록 후 상세 조회 시 결제 정보 없음
    coEvery { storeService.findStore(newStoreId) } returns StoreModel(id = newStoreId, storeName = "신규")

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r = client.post("/stores/regist") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "storeName": "신규",
          "businessNo": "111-11-11111"
        }
        """.trimIndent()
      )
    }
    assertEquals(HttpStatusCode.OK, r.status)
    // 결제 호출되지 않음
    coVerify(inverse = true) { billingService.paymentStoreBilling(any()) }
  }

  @Test
  fun `POST store modify should call service and payment`() = testApplication {
    application { testModule() }

    val storeId = "S-2000"
    coEvery { storeService.modifyStore(any(), any()) } returns Unit
    val billing = StoreBillingModel(id = 2L, storeUid = storeId, storeServiceSeq = 20, tokenUuid = UUID.randomUUID(), billingAmount = 2000, status = io.allink.receipt.api.common.BillingStatusCode.STANDBY, bankCode = null, bankAccountNo = null, bankAccountName = null, regBy = UUID.randomUUID(), regDate = LocalDateTime.parse("2025-06-02T00:00:00"))
    coEvery { storeService.findStore(storeId) } returns StoreModel(id = storeId, storeName = "변경", storeBilling = billing)
    coEvery { billingService.paymentStoreBilling(any()) } returns billing.copy(status = io.allink.receipt.api.common.BillingStatusCode.COMPLETE)

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r = client.post("/stores/modify") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "id": "$storeId",
          "storeName": "변경",
          "businessNo": "123-45-67890"
        }
        """.trimIndent()
      )
    }
    assertEquals(HttpStatusCode.OK, r.status)
    coVerify { storeService.modifyStore(any(), any()) }
    coVerify { billingService.paymentStoreBilling(any()) }
  }

  @Test
  fun `POST store modify should not call payment when no billing`() = testApplication {
    application { testModule() }

    val storeId = "S-2001"
    coEvery { storeService.modifyStore(any(), any()) } returns Unit
    coEvery { storeService.findStore(storeId) } returns StoreModel(id = storeId, storeName = "변경")

    val token = makeToken(mapOf("uUuid" to UUID.randomUUID().toString(), "role" to "MASTER"))
    val r = client.post("/stores/modify") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(
        """
        {
          "id": "$storeId",
          "storeName": "변경",
          "businessNo": "123-45-67890"
        }
        """.trimIndent()
      )
    }
    assertEquals(HttpStatusCode.OK, r.status)
    coVerify { storeService.modifyStore(any(), any()) }
    coVerify(inverse = true) { billingService.paymentStoreBilling(any()) }
  }

  @Test
  fun `Unauthorized requests should return 401`() = testApplication {
    application { testModule() }

    // /stores
    val r1 = client.post("/stores") { contentType(ContentType.Application.Json); setBody("{}") }
    assertEquals(HttpStatusCode.Unauthorized, r1.status)

    // /stores/detail/{id}
    val r2 = client.get("/stores/detail/S-0")
    assertEquals(HttpStatusCode.Unauthorized, r2.status)

    // /stores/search
    val r3 = client.post("/stores/search") { contentType(ContentType.Application.Json); setBody("{}") }
    assertEquals(HttpStatusCode.Unauthorized, r3.status)

    // /stores/billing-tokens/{businessNo}
    val r4 = client.get("/stores/billing-tokens/111-11-11111")
    assertEquals(HttpStatusCode.Unauthorized, r4.status)

    // /stores/regist
    val r5 = client.post("/stores/regist") { contentType(ContentType.Application.Json); setBody("{}") }
    assertEquals(HttpStatusCode.Unauthorized, r5.status)

    // /stores/modify
    val r6 = client.post("/stores/modify") { contentType(ContentType.Application.Json); setBody("{}") }
    assertEquals(HttpStatusCode.Unauthorized, r6.status)
  }
}
