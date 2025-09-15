package io.allink.receipt.api.domain.agency.bz

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

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

fun Route.agencyRoutes(
  bzAgencyService: BzAgencyService
) {

  route("/bz-agencies") {

    post("", {
      operationId = "bz-agencies"
      tags = listOf("Gestion des agences commerciales")
      summary = "Consulter la liste des agences commerciales"
      description = "Récupère la liste des agences commerciales."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<BzAgencyFilter>(agencyListRequest())
      }
      response {
        code(HttpStatusCode.OK, agencyListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val filter = call.receive<BzAgencyFilter>()
      call.respond(HttpStatusCode.OK, Response(data = bzAgencyService.getAgencies(filter)))
    }

    get("/detail/{agencyId}", {
      operationId = "bz-agencies-detail"
      tags = listOf("Gestion des agences commerciales")
      summary = "Consulter le détail d'une agence commerciale"
      description = "Récupère les informations détaillées d'une agence commerciale."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("agencyId") {
          description = "Identifiant de l'agence commerciale"
        }
      }
      response {
        code(HttpStatusCode.OK, agencyDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val agencyId = call.parameters["agencyId"] ?: ""
      call.respond(HttpStatusCode.OK, Response(data = bzAgencyService.getAgency(agencyId)))
    }

    post("/init", {
      operationId = "bz-agencies-init"
      tags = listOf("Gestion des agences commerciales")
      summary = "Générer l'UUID de l'agence commerciale"
      description = "Génère l'UUID de l'agence commerciale. Cette étape doit précéder toute création d'une nouvelle agence."
      securitySchemeNames = listOf("auth-jwt")
      response {
        code(HttpStatusCode.OK, {
          description = "Réponse réussie"
          body<Response<String>> {
            example("Exemple de réussite") {
              value = Response(
                data = "uuid-value-123-456"
              )
            }
          }
        })
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val createdAgency = bzAgencyService.createdAgency(userUuid)
      call.respond(HttpStatusCode.OK, Response(data = createdAgency))
    }

    post("/modify", {
      operationId = "bz-agencies-modify"
      tags = listOf("Gestion des agences commerciales")
      summary = "Enregistrer/mettre à jour les informations de l'agence commerciale"
      description = "Enregistre ou met à jour les informations de l'agence commerciale."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<BzAgencyModel>(agencyCreate())
      }
      response {
        code(HttpStatusCode.OK, agencyDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val agency = call.receive<BzAgencyModel>()
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val manageAgency = bzAgencyService.updateAgency(agency, userUuid)
      call.respond(HttpStatusCode.OK, Response(data = manageAgency))
    }

  }
}
