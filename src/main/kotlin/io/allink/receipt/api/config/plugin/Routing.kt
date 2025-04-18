package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.admin.adminRoutes
import io.allink.receipt.api.domain.code.serviceCodeRoutes
import io.allink.receipt.api.domain.login.loginRoutes
import io.allink.receipt.api.domain.receipt.issueReceiptRoutes
import io.allink.receipt.api.domain.store.storeRoutes
import io.allink.receipt.api.domain.user.userRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {

  install(DoubleReceive)
  install(SSE)

  routing {

    get("/") {
      call.respond("Allink receipt api")
    }

    loginRoutes(get())
    adminRoutes(get())

    authenticate("auth-jwt") {
      userRoutes(get())
      storeRoutes(get())
      serviceCodeRoutes(get())
      issueReceiptRoutes(get())
    }

    sse("/hello") {
      send(ServerSentEvent("world"))
    }
  }
}
