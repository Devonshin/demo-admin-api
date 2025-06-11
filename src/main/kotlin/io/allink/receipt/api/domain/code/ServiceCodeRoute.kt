package io.allink.receipt.api.domain.code

import io.allink.receipt.api.common.bankCodeListResponse
import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.franchiseCodeListResponse
import io.allink.receipt.api.common.vendorCodeListResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.response.*
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

  get("/service-code/banks", {
    operationId = "service-code-banks"
    tags = listOf("서비스 코드 관리")
    summary = "은행 코드 목록 조회"
    description = "은행 코드 목록을 조회합니다."
    securitySchemeNames = listOf("auth-jwt")

    response {
      code(HttpStatusCode.OK, bankCodeListResponse())
      code(HttpStatusCode.BadRequest, errorResponse())
    }
  }) {
    call.respond(
      HttpStatusCode.OK,
      Response(
        data = serviceCodeRepository.findAll(ServiceCodeGroup.BANK_CODE.name)
      )
    )
  }

  get("/service-code/vendors", {
    operationId = "service-code-vendors"
    tags = listOf("서비스 코드 관리")
    summary = "밴더사 목록 조회"
    description = "밴더사 코드 목록을 조회합니다."
    securitySchemeNames = listOf("auth-jwt")

    response {
      code(HttpStatusCode.OK, vendorCodeListResponse())
      code(HttpStatusCode.BadRequest, errorResponse())
    }
  }) {
    call.respond(
      HttpStatusCode.OK,
      Response(
        data = serviceCodeRepository.findAll(ServiceCodeGroup.VEN_CODE.name)
      )
    )
  }
}