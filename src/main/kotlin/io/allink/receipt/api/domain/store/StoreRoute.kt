package io.allink.receipt.api.domain.store

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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

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

    post("/search", {
      operationId = "stores-search"
      tags = listOf("가맹점 관리")
      summary = "매핑 용 가맹점 목록 조회"
      description = "매핑 용 가맹점 목록을 조회합니다. 예) 태그 등록 시"
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<StoreSearchFilter>(storeSearchRequest())
      }
      response {
        code(HttpStatusCode.OK, storeSearchResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val filter = call.receive<StoreSearchFilter>()
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = storeService.findSearchStores(filter)
        )
      )
    }

    //  storeName	businessNo	ceoName	addr1	addr2	franchiseCode	tel	managerName	email	phone	businessType	eventType	tagId
    /*
    * 가맹점 등록
    * */
    post("/regist", {
      operationId = "store-regist"
      tags = listOf("가맹점 관리")
      summary = "가맹점 등록"
      description = "새로운 가맹점을 등록합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<StoreRegistModel>(storeRegisterRequest())
      }
      response {
        code(HttpStatusCode.OK, storeDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val storeRegistModel = call.receive<StoreRegistModel>()
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val registStoreUid = storeService.registStore(storeRegistModel, UUID.fromString(userUuid))
      call.respond(HttpStatusCode.OK, Response(data = storeService.findStore(registStoreUid)))
    }

    /*
    * 가맹점 수정
    * */
    post("/modify", {
      operationId = "store-modify"
      tags = listOf("가맹점 관리")
      summary = "가맹점 수정"
      description = "가맹점 정보를 수정합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<StoreModifyModel>(storeModifyRequest())
      }
      response {
        code(HttpStatusCode.OK, storeDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val storeModifyModel = call.receive<StoreModifyModel>()
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      storeService.modifyStore(storeModifyModel, UUID.fromString(userUuid))
      call.respond(HttpStatusCode.OK, Response(data = storeService.findStore(storeModifyModel.id)))
    }


  }

}