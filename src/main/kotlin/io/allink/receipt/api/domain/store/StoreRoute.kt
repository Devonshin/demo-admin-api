package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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
        Response(data = storeService.findSearchStores(filter)
        )
      )
    }

    //  storeName	businessNo	ceoName	addr1	addr2	franchiseCode	tel	managerName	email	phone	businessType	eventType	tagId
    /*
    * 가맹점 등록
    * */
//    post("/register", {
//      operationId = "store-register"
//      tags = listOf("가맹점 관리")
//      summary = "가맹점 등록"
//      description = "새로운 가맹점을 등록합니다."
//      securitySchemeNames = listOf("auth-jwt")
//      request {
////        body<StoreModel>(storeRegisterRequest())
//      }
//      response {
//        code(HttpStatusCode.Created, storeRegisterResponse())
//        code(HttpStatusCode.BadRequest, errorResponse())
//      }
//    }) {
//      try {
//        val storeModel = call.receive<StoreModel>()
//        val registeredStore = storeService.registerStore(storeModel)
//        call.respond(HttpStatusCode.Created, Response(data = registeredStore))
//      } catch (e: Exception) {
//        logger.error("가맹점 등록 중 오류가 발생했습니다: ${e.message}", e)
//        call.respond(HttpStatusCode.BadRequest, errorResponse(message = "가맹점 등록 실패: ${e.localizedMessage}"))
//      }
//    }


  }
  
}