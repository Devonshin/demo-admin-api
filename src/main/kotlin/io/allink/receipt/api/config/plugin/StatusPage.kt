package io.allink.receipt.api.config.plugin

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.allink.receipt.api.common.ApiException
import io.allink.receipt.api.common.ErrorResponse
import io.allink.receipt.api.common.Response
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sns.model.NotFoundException

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureStatusPage() {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  install(StatusPages) {

    status(HttpStatusCode.NotFound) {call, status->
      call.respond(
        status,
        Response(
          ErrorResponse(
            code = status.value.toString(),
            message = status.description
          )
        )
      )
    }
    status(HttpStatusCode.Unauthorized) {call, status->
      call.respond(
        status,
        Response(
          ErrorResponse(
            code = status.value.toString(),
            message = status.description
          )
        )
      )
    }

    status(HttpStatusCode.MethodNotAllowed) {call, status->
      call.respond(
        status,
        Response(
          ErrorResponse(
            code = status.value.toString(),
            message = "HTTP 메소드가 유효하지 않음 :${status.description}"
          )
        )
      )
    }

    status(HttpStatusCode.UnsupportedMediaType) {call, status->
      call.respond(
        status,
        Response(
          ErrorResponse(
            code = status.value.toString(),
            message = "파라미터가 유효하지 않음 :${status.description}"
          )
        )
      )
    }

    exception<JWTDecodeException> { call, _ ->
      call.respond(
        HttpStatusCode.BadRequest,
        mapOf("error" to "Invalid token format")
      )
    }

    exception<TokenExpiredException> { call, _ ->
      call.respond(
        HttpStatusCode.Unauthorized,
        mapOf("error" to "Token has expired")
      )
    }

    exception<SignatureVerificationException> { call, _ ->
      call.respond(
        HttpStatusCode.Forbidden,
        mapOf("error" to "Invalid token signature")
      )
    }


    exception<RequestValidationException> { call, cause ->
      call.respond(
        HttpStatusCode.BadRequest,
        Response<ErrorResponse>(
          ErrorResponse(
            code = "400",
            message = cause.reasons.joinToString()
          )
        )
      )
    }

    exception<TokenExpiredException> { call, cause ->
      call.respond(
        HttpStatusCode.BadRequest,
        Response<ErrorResponse>(
          ErrorResponse(
            code = "401",
            message = "Token expired"
          )
        )
      )
    }

    exception<JsonConvertException> { call, cause ->
      val message = when (val realCause = cause.cause) {
        is MissingFieldException -> {
          val fields = realCause.missingFields.joinToString(", ")
          "Missing required field(s): $fields"
        }
        else -> "Invalid JSON format: ${cause.message}"
      }

      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "400",
            message = message
          )
        )
      )
    }

    exception<BadRequestException> { call, cause ->
      logger.error("Bad Request : {}", cause.message)
      call.respond(
        HttpStatusCode.BadRequest,
        Response<ErrorResponse>(
          ErrorResponse(
            code = "400",
            message = "Bad Request: ${cause.message}"
          )
        )
      )
    }

    exception<NotFoundException> { call, cause ->
      call.respond(
        HttpStatusCode.NotFound,
        Response<ErrorResponse>(
          ErrorResponse(
            code = "400",
            message = "Resource not found"
          )
        )
      )
    }

    exception<ApiException> { call, cause ->
      call.respond(
        HttpStatusCode.BadRequest,
        Response<ErrorResponse>(
          ErrorResponse(
            code = cause.code,
            message = cause.message
          )
        )
      )
    }

    exception<Throwable> { call, cause ->
      logger.error("Unhandled exception", cause)
      call.respond(
        status = HttpStatusCode.InternalServerError, message = Response<ErrorResponse>(
          ErrorResponse(
            code = "500",
            message = "Unknown error"
          )
        )
      )
    }
  }
}
