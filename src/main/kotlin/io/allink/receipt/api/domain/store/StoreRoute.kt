package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.domain.admin.BzAgencyMasterRole
import io.allink.receipt.api.domain.admin.BzAgencyStaffRole
import io.allink.receipt.api.domain.admin.toRole
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
  storeService: StoreService,
  storeBillingService: StoreBillingService
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
      val principal: JWTPrincipal = call.principal()!!
      val payload = principal.payload
      val role = payload.getClaim("role").asString()
      val toRole = role.toRole()

      val stores = if (toRole is BzAgencyMasterRole || toRole is BzAgencyStaffRole) {
        storeService.findAllAgencyStore(filter, UUID.fromString(payload.getClaim("agencyId").asString()))
      } else storeService.findAllStore(filter)

      call.respond(HttpStatusCode.OK, Response(data = stores))
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
      val principal: JWTPrincipal = call.principal()!!
      val payload = principal.payload
      val role = payload.getClaim("role").asString()
      val toRole = role.toRole()
      val store = if (toRole is BzAgencyMasterRole || toRole is BzAgencyStaffRole) {
        storeService.findStore(id, UUID.fromString(payload.getClaim("agencyId").asString()))
      } else storeService.findStore(id)

      call.respond(
        HttpStatusCode.OK,
        Response(
          data = store
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
      description = "새로운 가맹점을 등록합니다. 이용 서비스가 있을 경우 결제 정보는 필수입니다. 등록과 즉시 결제가 이루어지며 결제 실패 시 modify 인터페이스로 재결제 요청을 보내야 합니다."
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
      logger.info("Received storeRegistModel request: $storeRegistModel")
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val payload = principal.payload
      val role = payload.getClaim("role").asString()
      val toRole = role.toRole()
      val registStoreUid = if (toRole is BzAgencyMasterRole) {
        storeService.registStore(
          storeRegistModel.copy(bzAgencyId = payload.getClaim("agencyId").asString()),UUID.fromString(userUuid))
      } else {
        storeService.registStore(storeRegistModel, UUID.fromString(userUuid))
      }
      val registeredStore = storeService.findStore(registStoreUid)
      registeredStore?.storeBilling?.let {
        registeredStore.storeBilling = storeBillingService.paymentStoreBilling(it)
      }
      call.respond(HttpStatusCode.OK, Response(data = registeredStore))
    }

    /*
    * 가맹점 수정
    * */
    post("/modify", {
      operationId = "store-modify"
      tags = listOf("가맹점 관리")
      summary = "가맹점 수정"
      description = "가맹점 정보를 수정합니다. 이용 서비스가 변경된 경우 변경된 서비스는 결제정보의 상태가 STANDBY: 즉시 결제, PENDING:  익월 1일 결제. 서비스는 결제 성공 후에 반영이 됩니다."
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
      logger.info("Received storeModifyModel request: $storeModifyModel")
      val principal: JWTPrincipal = call.principal()!!
      val userUuid = principal.payload.getClaim("uUuid").asString()
      val payload = principal.payload
      val role = payload.getClaim("role").asString()
      val toRole = role.toRole()
      if (toRole is BzAgencyMasterRole) {
        storeService.modifyStore(
          storeModifyModel.copy(bzAgencyId = payload.getClaim("agencyId").asString()),UUID.fromString(userUuid))
      } else {
        storeService.modifyStore(storeModifyModel, UUID.fromString(userUuid))
      }
      val modifiedStore = storeService.findStore(storeModifyModel.id)
      modifiedStore?.storeBilling?.let {
        modifiedStore.storeBilling = storeBillingService.paymentStoreBilling(it)
      }

      call.respond(HttpStatusCode.OK, Response(data = modifiedStore))
    }


    get("/billing-tokens/{businiessNo}", {
      operationId = "store-detail"
      tags = listOf("가맹점 관리")
      summary = "사업자 번호로 카드 토큰 조회"
      description = "사업자 번호로 카드 정보를 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("businiessNo") {
          description = "사업자번호"
          example("businessNo") {
            value = "123-45-67890"
          }
        }
      }
      response {
        code(HttpStatusCode.OK, storeBillingTokenListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val businessNo = call.request.pathVariables["businiessNo"] ?: ""
      call.respond(
        HttpStatusCode.OK,
        Response(
          data = storeService.findAllBillingToken(businessNo)
        )
      )
    }

  }

}