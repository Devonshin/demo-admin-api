package io.allink.receipt.api

import io.allink.receipt.api.config.plugin.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.module() {
  configureHTTP()
  configureMonitoring()
  configureStatusPage()
  configureValidation()
  configureSerialization()
  configureDatabases()
  configureFrameworks()
  configureSecurity()
  configureRouting()
  configureOpenApi()
}
