package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.domain.login.VerificationCode
import io.allink.receipt.api.domain.login.VerificationCodeRequest
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorredoc.redoc
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureOpenApi() {

  install(OpenApi) {

    tags {
      tag("Receipt Admin API") {
        description = "Receipt Admin API"
      }
    }

    security {

      securityScheme("auth-jwt") {
        name = "Jwt 인증"
        description = "Authorization: Bearer {{JWT}}"
        type = AuthType.HTTP
        scheme = AuthScheme.BEARER
        bearerFormat = "JWT"
      }

    }

    pathFilter = { method, url ->
      val excludeUrl = url.firstOrNull()
      when (excludeUrl) {
        "openapi" -> false
        "swagger" -> false
        "doc" -> false
        "health" -> false
        "ready" -> false
        "api" -> false
        "hello" -> false
        null -> false
        else -> true
      }
    }

    info {

      title = "Demo Receipt API"
      version = "1.0.0"
      description = "API for the allink receipt administration."
      contact {
        name = "Demo"
        url = "https://allink.io"
        email = "dw.shin@allink.io"
      }
    }

    server {
      description = "로컬"
      url = "http://localhost:8080"
    }

    schemas {
      schema<VerificationCodeRequest>("verification-code-request")
      schema<Response<VerificationCode>>("verification-code-response")
    }

  }

  routing {

    route(".api.json") {
      openApi()
    }

    route("/doc") {
      redoc("/.api.json", {
        hideDownloadButton = true
        showObjectSchemaExamples = true
      })
    }

    swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
      version = "4.15.5"
    }

  }
}
