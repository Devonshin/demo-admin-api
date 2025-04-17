package io.allink.receipt.api.domain.code

import io.allink.receipt.api.common.Response
import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.franchiseCodeListResponse
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

/**
 * Package: io.allink.receipt.api.domain.code
 * Created: Devonshin
 * Date: 17/04/2025
 */

fun Route.serviceCodeRoutes(
  serviceCodeRepository: ServiceCodeRepository
) {

  get("/service-code/franchise", {
    operationId = "service-code-franchise"
    tags = listOf("서비스 코드 관리")
    summary = "프랜차이즈 코드 목록 조회"
    description = "프랜차이즈 코드 목록을 조회합니다."
    securitySchemeNames = listOf("auth-jwt")

    response {
      code(HttpStatusCode.OK, franchiseCodeListResponse())
      code(HttpStatusCode.BadRequest, errorResponse())
    }
  }) {
    call.respond(
      HttpStatusCode.OK,
      Response(
        data = serviceCodeRepository.findAll(ServiceCodeGroup.FRANCHISE.name)
      )
    )
  }
}