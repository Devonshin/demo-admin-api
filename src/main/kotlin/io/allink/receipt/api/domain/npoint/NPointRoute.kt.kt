package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.pointListRequest
import io.allink.receipt.api.common.pointListResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Package: io.allink.receipt.api.domain.point
 * Created: Devonshin
 * Date: 20/05/2025
 */

fun Route.pointRoutes(
  nPointService: NPointService
) {

  route("/points") {
    get("", {
      operationId = "points"
      tags = listOf("포인트 관리")
      summary = "포인트 지급 목록 조회"
      description = "포인트 지급 목록을 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<NPointFilter>(pointListRequest())
      }
      response {
        code(HttpStatusCode.OK, pointListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }

    }) {
      val filter = call.receive<NPointFilter>()
      call.respond(
        HttpStatusCode.OK,
        Response(data = nPointService.getAllNPointPay(filter))
      )
    }
  }

}