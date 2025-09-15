package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.userDetailResponse
import io.allink.receipt.api.common.userListRequest
import io.allink.receipt.api.common.userListResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */

fun Route.userRoutes(
  userService: UserService
) {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  route("/users") {

    post("", {
      operationId = "users"
      tags = listOf("Gestion des utilisateurs")
      summary = "Consulter la liste des utilisateurs"
      description = "Récupère la liste des utilisateurs."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<UserFilter>(userListRequest())
      }
      response {
        code(HttpStatusCode.OK, userListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {

      val filter = call.receive<UserFilter>()
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = userService.findAllUser(filter)
        )
      )
    }

    get("/detail/{userId}", {
      operationId = "user-detail"
      tags = listOf("Gestion des utilisateurs")
      summary = "Consulter le détail de l'utilisateur"
      description = "Récupère les informations détaillées de l'utilisateur."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("userId") {
          description = "Identifiant de l'utilisateur"
        }
      }

      response {
        code(HttpStatusCode.OK, userDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {

      val id = call.request.pathVariables["userId"] ?: ""
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = userService.findUser(id)
        )
      )
    }
  }

}