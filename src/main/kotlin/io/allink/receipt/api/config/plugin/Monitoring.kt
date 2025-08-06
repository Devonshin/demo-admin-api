package io.allink.receipt.api.config.plugin

import dev.hayden.KHealth
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun Application.configureMonitoring() {
  install(CallId) {
    header(HttpHeaders.XRequestId)
    verify { callId: String ->
      callId.isNotEmpty()
    }
    generate { java.util.UUID.randomUUID().toString() }
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

  intercept(ApplicationCallPipeline.Monitoring) {
    call.response.pipeline.intercept(ApplicationSendPipeline.After) { subject ->
      if (subject is TextContent
        && subject.contentType.withoutParameters().match(ContentType.Application.Json)
      ) {
        val jsonResponse = Json.parseToJsonElement(subject.text)
        this@configureMonitoring.log.info("$jsonResponse")
      }
    }
  }

}
