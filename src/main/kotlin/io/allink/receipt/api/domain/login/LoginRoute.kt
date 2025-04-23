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
      tags = listOf("로그인")
      summary = "휴대폰으로 인증코드 요청"
      description = "등록된 휴대폰으로 로그인용 인증코드로 발송합니다."
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
      tags = listOf("로그인")
      summary = "인증 코드로 JWT 요청"
      description = "휴대폰으로 수신한 인증코드를 전송하여 JWT를 획득합니다."
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
        Response<Jwt>(
          data = jwt
        )
      )
    }


    authenticate("auth-jwt") {
      get("/jwt-renewal-request", {
        tags = listOf("로그인")
        securitySchemeNames = listOf("auth-jwt")
        operationId = "jwt-renewal-request"
        summary = "JWT 갱신"
        description = "JWT 가 만료되기 전에 갱신 요청을 할 수 있습니다."
        response {
          code(HttpStatusCode.OK, verificationCodeCheckResponse())
          code(HttpStatusCode.BadRequest, errorResponse())
        }
      }) {

        val principal: JWTPrincipal = call.principal()!!
        val jwt = loginService.renewalJwt(principal)

        call.respond(
          HttpStatusCode.OK,
          Response<Jwt>(
            data = jwt
          )
        )
      }
    }
  }
}
