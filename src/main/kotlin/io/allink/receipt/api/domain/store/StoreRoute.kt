package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.Response
import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.storeDetailResponse
import io.allink.receipt.api.common.storeListRequest
import io.allink.receipt.api.common.storeListResponse
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

fun Route.storeRoutes(
  storeService: StoreService
) {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  route("/stores") {
    post("", {
      operationId = "stores"
      tags = listOf("가맹점 관리")
      summary = "가맹점 목록 조회"
      description = "가맹점 목록을 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<StoreFilter>(storeListRequest())
      }
      response {
        code(HttpStatusCode.OK, storeListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val filter = call.receive<StoreFilter>()
      call.respond(HttpStatusCode.OK, Response(data = storeService.findAllStore(filter)))
    }

    get("/detail/{storeId}", {
      operationId = "store-detail"
      tags = listOf("가맹점 관리")
      summary = "가맹점 상세 조회"
      description = "가맹점 상세 정보를 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("storeId") {
          description = "가맹점 id"
        }
      }
      response {
        code(HttpStatusCode.OK, storeDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val id = call.request.pathVariables["storeId"] ?: ""
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = storeService.findStore(id)
        )
      )
    }

  }
}