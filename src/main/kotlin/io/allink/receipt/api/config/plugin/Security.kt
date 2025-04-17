package io.allink.receipt.api.config.plugin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.allink.receipt.api.common.ErrorResponse
import io.allink.receipt.api.common.Response
import io.allink.receipt.api.domain.admin.AdminRepository
import io.allink.receipt.api.domain.admin.AdminStatus
import io.allink.receipt.api.domain.login.LoginInfoRepository
import io.allink.receipt.api.domain.login.LoginStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.get
import java.util.*

fun Application.configureSecurity() {
  // Please read the jwt property from the config file if you are using EngineMain
  val config = environment.config
  val jwtAudience = config.property("jwt.audience").getString()
  val jwtDomain = config.property("jwt.domain").getString()
  val jwtRealm = config.property("jwt.realm").getString()
  val jwtSecret = config.property("jwt.secret").getString()

  authentication {
    jwt("auth-jwt") {
      realm = jwtRealm
      verifier(
        JWT
          .require(Algorithm.HMAC256(jwtSecret))
          .withAudience(jwtAudience)
          .withIssuer(jwtDomain)
          .build()
      )
      validate { credential ->

        val payload = credential.payload
        val loginUuid = payload.getClaim("lUuid").asString()
        if (loginUuid == null) return@validate null
        val loginInfoRepository: LoginInfoRepository = get()

        val loginInfo = loginInfoRepository.find(UUID.fromString(loginUuid))
        if (loginInfo == null) return@validate null
        if (loginInfo.status != LoginStatus.ACTIVE) return@validate null

        val userUuid = payload.getClaim("uUuid").asString()
        if (userUuid == null) return@validate null

        var adminRepository: AdminRepository = get()
        val admin = adminRepository.findByUserUuid(UUID.fromString(userUuid))
        if (admin == null) return@validate null
        if (admin.status != AdminStatus.ACTIVE) return@validate null

//        todo 권한 체크 추가

        if (payload.audience.contains(jwtAudience)) {
          JWTPrincipal(payload)
        }
        if (payload.getClaim("username").asString() != "") {
          JWTPrincipal(credential.payload)
        } else null
      }
      challenge { defaultScheme, realm ->
        call.respond(
          HttpStatusCode.Unauthorized,
          Response<ErrorResponse>(
            ErrorResponse(
              code = "TOKEN_EXPIRED",
              message = "Token expired"
            )
          )
        )
      }
    }
  }

}
