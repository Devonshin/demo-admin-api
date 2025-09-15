package io.allink.receipt.api.domain.login

import io.allink.receipt.api.common.*
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Package: io.allink.receipt.admin.domain.admin
 * Created: Devonshin
 * Date: 13/04/2025
 */

fun Route.loginRoutes(
  loginService: LoginService
) {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  route("/login") {
    post("/verification-code-request", {
      operationId = "login-verification-code-request"
      tags = listOf("Connexion")
      summary = "Demander un code de vérification par téléphone mobile"
      description = "Envoie un code de vérification pour la connexion au numéro de téléphone enregistré."
      request {
        body<VerificationCodeRequest>(verificationCodeRequest())
      }
      response {
        code(HttpStatusCode.OK, block = verificationCodeResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val verificationCodeRequest = call.receive<VerificationCodeRequest>()
      logger.info("Received verificationCodeRequest request: $verificationCodeRequest")

      val generateVerificationCode = loginService.generateVerificationCode(verificationCodeRequest)

      call.respond(
        HttpStatusCode.OK,
        Response(
          data = generateVerificationCode
        )
      )
    }

    post("/verification-code-check", {
      operationId = "login-verification-code-check"
      tags = listOf("Connexion")
      summary = "Demander un JWT avec le code de vérification"
      description = "Envoyez le code de vérification reçu sur votre téléphone pour obtenir un JWT."
      request {
        body<VerificationCheckRequest>(verificationCodeCheckRequest())
      }
      response {
        code(HttpStatusCode.OK, verificationCodeCheckResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {

      val verificationCheckRequest = call.receive<VerificationCheckRequest>()
      val jwt = loginService.checkVerificationCode(verificationCheckRequest)

      call.respond(
        HttpStatusCode.OK,
        Response(
          data = jwt
        )
      )
    }


    authenticate("auth-jwt") {
      get("/jwt-renewal-request", {
        tags = listOf("Connexion")
        securitySchemeNames = listOf("auth-jwt")
        operationId = "jwt-renewal-request"
        summary = "Renouvellement du JWT"
        description = "Permet de demander le renouvellement du JWT avant son expiration."
        response {
          code(HttpStatusCode.OK, verificationCodeCheckResponse())
          code(HttpStatusCode.BadRequest, errorResponse())
        }
      }) {

        val principal: JWTPrincipal = call.principal()!!
        val jwt = loginService.renewalJwt(principal)

        call.respond(
          HttpStatusCode.OK,
          Response(
            data = jwt
          )
        )
      }
    }
  }
}
