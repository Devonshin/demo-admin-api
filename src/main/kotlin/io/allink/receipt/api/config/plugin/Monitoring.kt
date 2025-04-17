package io.allink.receipt.api.config.plugin

import dev.hayden.KHealth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
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
    callIdMdc("call-id")
    level = Level.INFO
    format { call ->
      val status = call.response.status()
      val httpMethod = call.request.httpMethod.value
      val userAgent = call.request.headers["User-Agent"]
      "Status: $status, HTTP method: $httpMethod, User agent: $userAgent"
    }

    filter { call ->
      !call.request.path().startsWith("/health")
    }
  }
}
