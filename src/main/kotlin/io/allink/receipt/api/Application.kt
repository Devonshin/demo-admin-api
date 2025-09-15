package io.allink.receipt.api

import io.allink.receipt.api.config.plugin.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.netty.*


fun main(args: Array<String>) {
  val dotenv = dotenv {
    ignoreIfMissing = true
  }
  dotenv.entries().forEach {
    System.setProperty(it.key, it.value)
  }
  System.setProperty("java.net.preferIPv4Stack", "true")
  System.setProperty("java.net.preferIPv6Addresses", "false")
  EngineMain.main(args)
}

fun Application.module() {
  configureMonitoring()
  configureStatusPage()
  configureValidation()
  configureSerialization()
  configureDatabases()
  configureFrameworks()
  configureHTTP()
  configureSecurity()
  configureRouting()
  configureOpenApi()
}
