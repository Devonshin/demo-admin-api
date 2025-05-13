package io.allink.receipt.api.config.plugin

import dev.hayden.KHealth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.uri
import org.slf4j.event.Level

fun Application.configureMonitoring() {
  install(CallId) {
    header(HttpHeaders.XRequestId)
    verify { callId: String ->
      callId.isNotEmpty()
    }
  }
  install(KHealth)
  install(CallLogging) {
    filter { call ->
      val uri = call.request.uri
      uri != "/"
    }
    callIdMdc("call-id")
    level = Level.INFO
    format { call ->
      val status = call.response.status()
      val httpMethod = call.request.httpMethod.value
      val userAgent = call.request.headers["User-Agent"]
      val uri = call.request.uri

      "URI: $uri, Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
    }
  }
}
