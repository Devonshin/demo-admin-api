package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

fun Route.agencyRoutes(
  bzAgencyService: BzAgencyService
) {

  route("/bz-agencies") {

    post("", {
      operationId = "bz-agencies"
      tags = listOf("영업 대리점 관리")
      summary = "영업 대리점 목록 조회"
      description = "영업 대리점 목록을 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<BzAgencyFilter>(agencyListRequest())
      }
      response {
        code(HttpStatusCode.OK, agencyListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val filter = call.receive<BzAgencyFilter>()
      call.respond(HttpStatusCode.OK, Response(data = bzAgencyService.getAgencies(filter)))
    }

    get("/detail/{agencyId}", {
      operationId = "bz-agencies-detail"
      tags = listOf("영업 대리점 관리")
      summary = "영업 대리점 상세 조회"
      description = "영업 대리점 상세 정보를 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("agencyId") {
          description = "영업 대리점 아이디"
        }
      }
      response {
        code(HttpStatusCode.OK, agencyDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val agencyId = call.parameters["agencyId"] ?: ""
      call.respond(HttpStatusCode.OK, Response(data = bzAgencyService.getAgency(agencyId)))
    }

    post("/init", {
      operationId = "bz-agencies-init"
      tags = listOf("영업 대리점 관리")
      summary = "영업 대리점 uuid 생성"
      description = "영업 대리점의 uuid 를 생성합니다. 영업 대리점 신규 생성 시 반드시 선행 되어야 합니다."
      securitySchemeNames = listOf("auth-jwt")
      response {
        code(HttpStatusCode.OK, {
          description = "성공 응답"
          body<Response<String>> {
            example("실패 응답") {
              value = Response(
                data = "uuid-value-123-456"
              )
            }
          }
        })
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val createdAgency = bzAgencyService.createdAgency(userUuid)
      call.respond(HttpStatusCode.OK, Response(data = createdAgency))
    }

    post("/modify", {
      operationId = "bz-agencies-modify"
      tags = listOf("영업 대리점 관리")
      summary = "영업 대리점 정보 등록/수정"
      description = "영업 대리점 정보를 등록/수정합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<BzAgencyModel>(agencyCreate())
      }
      response {
        code(HttpStatusCode.OK, agencyDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val agency = call.receive<BzAgencyModel>()
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val manageAgency = bzAgencyService.updateAgency(agency, userUuid)
      call.respond(HttpStatusCode.OK, Response(data = manageAgency))
    }

  }
}
