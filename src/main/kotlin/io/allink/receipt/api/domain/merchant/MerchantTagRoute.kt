package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 25/04/2025
 */

fun Route.merchantTagRoutes(
  merchantTagService: MerchantTagService
) {
  route("/tags") {
    post("", {
      operationId = "tags"
      tags = listOf("Gestion des tags")
      summary = "Consulter la liste des tags"
      description = "Récupère la liste des tags."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<MerchantTagFilter>(tagListRequest())
      }
      response {
        code(HttpStatusCode.OK, tagListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }

    }) {

      val filter = call.receive<MerchantTagFilter>()
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = merchantTagService.getTags(filter)
        )
      )
    }

    get("/detail/{tagId}", {
      operationId = "tags-detail"
      tags = listOf("Gestion des tags")
      summary = "Consulter le détail du tag"
      description = "Récupère les informations détaillées du tag."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("tagId") {
          description = "Identifiant du tag"
        }
      }
      response {
        code(HttpStatusCode.OK, tagDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }

    }) {
      val tagId = call.pathParameters["tagId"] ?: ""
      call.respond(Response(data = merchantTagService.getTag(tagId)))
    }

    post("/modify", {
      operationId = "tags-modify"
      tags = listOf("Gestion des tags")
      summary = "Enregistrer/modifier un tag"
      description = "Enregistre ou modifie les informations d’un tag."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<MerchantTagModifyModel>(tagModifyRequest())
      }
      response {
        code(HttpStatusCode.OK, tagDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }

    }) {

      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val modify = call.receive<MerchantTagModifyModel>()
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = merchantTagService.modifyTag(modify, UUID.fromString(userUuid))
        )
      )
    }
  }

}