package io.allink.receipt.api.config.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.configureHTTP() {
  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
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

    logger.info("env = $env")
    if (env != null && !env.equals("production", ignoreCase = true)) {
      anyHost()
    } else {
      allowHost("allink.io", schemes = listOf("http", "https"))
      allowHost("dev-receipt-admin.allink.io", schemes = listOf("https"))
      allowHost("receipt-admin.allink.io", schemes = listOf("https"))
    }
  }
  install(Compression)
  routing {
    swaggerUI(path = "openapi")
  }
}
