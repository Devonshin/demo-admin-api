package io.allink.receipt.api.config.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
  val env = environment.config.propertyOrNull("ktor.environment")?.getString()?.let {
    it.lowercase()
  }
  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowMethod(HttpMethod.Put)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
    allowCredentials = true // 쿠키 및 인증 정보 전송 허용 여부
    if (env != null && !env.contains("production")) {
      println("env = $env")
      anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
  }
  install(Compression)
  routing {
    swaggerUI(path = "openapi")
  }
}
