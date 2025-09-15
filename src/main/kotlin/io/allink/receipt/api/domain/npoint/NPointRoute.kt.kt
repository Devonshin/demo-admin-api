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
      tags = listOf("Gestion des points")
      summary = "Consulter la liste des versements de points"
      description = "Récupère la liste des versements de points."
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