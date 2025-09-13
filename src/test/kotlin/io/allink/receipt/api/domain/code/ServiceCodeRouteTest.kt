package io.allink.receipt.api.domain.code

import io.allink.receipt.api.domain.Response
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @file ServiceCodeRouteTest.kt
 * @brief ServiceCode 라우트 테스트 - 그룹별 코드 목록 조회 응답 구조 검증
 */
class ServiceCodeRouteTest {

  private val repository: ServiceCodeRepository = mockk(relaxed = true)

  private fun Application.testModule() {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true; isLenient = true; coerceInputValues = true }) }
    routing { serviceCodeRoutes(repository) }
  }

  @Test
  fun `GET service-code endpoints should return lists`() = testApplication {
    application { testModule() }

    coEvery { repository.findAll(ServiceCodeGroup.FRANCHISE.name) } returns listOf(
      ServiceCodeModel(id = "EDIYA", serviceGroup = "FRANCHISE", serviceName = "이디야", price = 0, status = ServiceCodeStatus.ACTIVE, serviceType = null)
    )
    coEvery { repository.findAll(ServiceCodeGroup.BANK_CODE.name) } returns listOf(
      ServiceCodeModel(id = "NONG", serviceGroup = "BANK_CODE", serviceName = "농협", price = null, status = ServiceCodeStatus.ACTIVE, serviceType = null)
    )
    coEvery { repository.findAll(ServiceCodeGroup.VEN_CODE.name) } returns listOf(
      ServiceCodeModel(id = "KOCES", serviceGroup = "VEN_CODE", serviceName = "케이오시스", price = null, status = ServiceCodeStatus.ACTIVE, serviceType = null)
    )

    val r1 = client.get("/service-code/franchise")
    val body1 = r1.bodyAsText()
    assertEquals(HttpStatusCode.OK, r1.status)
    // data는 배열(List)로 직렬화되므로 포함 여부로 검증
    kotlin.test.assertTrue(body1.contains("EDIYA"))

    val r2 = client.get("/service-code/banks")
    assertEquals(HttpStatusCode.OK, r2.status)

    val r3 = client.get("/service-code/vendors")
    assertEquals(HttpStatusCode.OK, r3.status)
  }
}