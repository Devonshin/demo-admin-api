package io.allink.receipt.api.config.plugin

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.allink.receipt.api.domain.ErrorResponse
import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.exception.ApiException
import io.ktor.http.*
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureStatusPage() {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  install(StatusPages) {

    status(HttpStatusCode.NotFound) { call, status ->
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

    status(HttpStatusCode.Unauthorized) { call, status ->
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

    status(HttpStatusCode.MethodNotAllowed) { call, status ->
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

    status(HttpStatusCode.UnsupportedMediaType) { call, status ->
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

    exception<ApiException> { call, cause ->
      logger.error("ApiException : {}", cause.message, cause)
      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = cause.code,
            message = cause.message
          )
        )
      )
    }

    exception<JWTDecodeException> { call, _ ->
      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = "Invalid token format"
          )
        )
      )
    }


    exception<SignatureVerificationException> { call, _ ->
      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = "Invalid token format"
          )
        )
      )
    }


    exception<RequestValidationException> { call, cause ->
      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = cause.reasons.joinToString()
          )
        )
      )
    }

    exception<TokenExpiredException> { call, cause ->
      call.respond(
        HttpStatusCode.Unauthorized,
        Response(
          ErrorResponse(
            code = "UNAUTHORIZED",
            message = "Token expired"
          )
        )
      )
    }

    exception<ContentTransformationException> { call, cause ->
      logger.error("ContentTransformationException : {}", cause.message, cause)
      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = "요청 본문이 잘못 됐습니다. 필드를 확인하세요.[${cause.message}]"
          )
        )
      )
    }

    exception<BadRequestException> { call, cause ->
      logger.error("BadRequestException : {}", cause.message, cause)
      val rootCause = cause.cause

      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = "Bad Request: ${rootCause?.message?:cause.message}"
          )
        )
      )
    }

    exception<ExposedSQLException> { call, cause ->
      logger.error("ExposedSQLException : {}", cause.message, cause)
      call.respond(
        HttpStatusCode.BadRequest,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = "Bad Request: ${cause.message}"
          )
        )
      )
    }

    exception<NotFoundException> { call, cause ->
      call.respond(
        HttpStatusCode.NotFound,
        Response(
          ErrorResponse(
            code = "BAD_REQUEST",
            message = "Resource not found [${cause.message}]"
          )
        )
      )
    }

    exception<Throwable> { call, cause ->
      logger.error("Unhandled exception", cause)
      val rootCause = cause.cause
      call.respond(
        status = HttpStatusCode.InternalServerError, message = Response(
          ErrorResponse(
            code = "INTERNAL_SERVER_ERROR",
            message = "${rootCause?.message?:cause.message}"
          )
        )
      )
    }
  }
}
