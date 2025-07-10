package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.admin.Role
import io.allink.receipt.api.domain.admin.adminRoutes
import io.allink.receipt.api.domain.admin.toRole
import io.allink.receipt.api.domain.agency.bz.agencyRoutes
import io.allink.receipt.api.domain.code.serviceCodeRoutes
import io.allink.receipt.api.domain.file.fileRoutes
import io.allink.receipt.api.domain.login.loginRoutes
import io.allink.receipt.api.domain.merchant.merchantTagRoutes
import io.allink.receipt.api.domain.npoint.pointRoutes
import io.allink.receipt.api.domain.receipt.issueReceiptRoutes
import io.allink.receipt.api.domain.store.storeRoutes
import io.allink.receipt.api.domain.user.userRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import org.koin.ktor.ext.get
import java.lang.Thread.sleep

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
      install(RoleAuthorizationPlugin) {
        roleProvider = { call ->
          call.principal<JWTPrincipal>()
            ?.payload
            ?.getClaim("role")
            ?.asString()
            ?.toRole()
        }
      }
      userRoutes(get())
      storeRoutes(get(), get())
      serviceCodeRoutes(get())
      issueReceiptRoutes(get())
      merchantTagRoutes(get())
      pointRoutes(get())
      agencyRoutes(get())
      fileRoutes(get())
    }

    sse("/hello") {
      send(ServerSentEvent("world"))
      sleep(1000)
      send(ServerSentEvent("world2"))
    }
  }
}

class RoleAuthorizationConfig {
  var roleProvider: ((ApplicationCall) -> Role?) = {
    throw IllegalStateException("Role provider logic is not implemented.")
  }
}

val RoleAuthorizationPlugin = createRouteScopedPlugin(
  name = "RoleAuthorization",
  createConfiguration = ::RoleAuthorizationConfig
) {
  val roleProvider = pluginConfig.roleProvider
  onCall { call ->
    checkRoleAuthorization(call, roleProvider)
  }
}

private suspend fun checkRoleAuthorization(call: ApplicationCall, roleProvider: (ApplicationCall) -> Role?) {
  val role = roleProvider(call)
  if (role == null) {
    return
  }
  val requestedUri = call.request.uri.trimStart('/')

  if (!role.menus.any { it.path.equals(requestedUri, ignoreCase = true) }) {
    call.respond(HttpStatusCode.Forbidden, "접근 권한이 없습니다.")
    return
  }
}
