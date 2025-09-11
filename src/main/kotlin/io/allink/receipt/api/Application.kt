package io.allink.receipt.api

import io.allink.receipt.api.config.plugin.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.github.cdimascio.dotenv.dotenv


fun main(args: Array<String>) {
  val dotenv = dotenv {
    ignoreIfMissing = true
  }
  dotenv.entries().forEach {
    System.setProperty(it.key, it.value)
  }
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
