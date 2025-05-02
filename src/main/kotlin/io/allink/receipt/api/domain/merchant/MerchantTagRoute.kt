package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.tagDetailResponse
import io.allink.receipt.api.common.tagListRequest
import io.allink.receipt.api.common.tagListResponse
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 25/04/2025
 */

fun Route.merchantTagRoutes(
  merchantTagService: MerchantTagService
) {


  authenticate("auth-jwt") {
    route("/tags") {
      post("", {
        operationId = "tags"
        tags = listOf("태그 관리")
        summary = "태그 목록 조회"
        description = "태그 목록을 조회합니다."
        securitySchemeNames = listOf("auth-jwt")
        request {
          body<MerchantTagFilter>(tagListRequest())
        }
        response {
          code(HttpStatusCode.OK, tagListResponse())
          code(HttpStatusCode.BadRequest, errorResponse())
        }

      }) {
        val filter = call.receive<MerchantTagFilter>()
        call.respond(merchantTagService.getTags(filter))
      }

      get("/detail/{tagId}", {
        operationId = "tags-detail"
        tags = listOf("태그 관리")
        summary = "태그 상세 조회"
        description = "태그 상세 정보를 조회합니다."
        securitySchemeNames = listOf("auth-jwt")
        request {
          pathParameter<String>("tagId") {
            description = "태그 아이디"
          }
        }
        response {
          code(HttpStatusCode.OK, tagDetailResponse())
          code(HttpStatusCode.BadRequest, errorResponse())
        }

      }) {
        val tagId = call.pathParameters["tagId"] ?: ""
        call.respond(merchantTagService.getTag(tagId))
      }

    }
  }
}