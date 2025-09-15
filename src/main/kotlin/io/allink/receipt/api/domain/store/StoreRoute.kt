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
      tags = listOf("Gestion des commerçants")
      summary = "Consulter la liste des commerçants"
      description = "Récupère la liste des commerçants."
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
      tags = listOf("Gestion des commerçants")
      summary = "Consulter le détail du commerçant"
      description = "Récupère les informations détaillées du commerçant."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("storeId") {
          description = "Identifiant du commerçant"
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
      tags = listOf("Gestion des commerçants")
      summary = "Consulter la liste des commerçants pour le mapping"
      description = "Récupère la liste des commerçants à des fins de mapping, p.ex. lors de l’enregistrement d’un tag."
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
      tags = listOf("Gestion des commerçants")
      summary = "Enregistrer un commerçant"
      description =
        "Enregistre un nouveau commerçant. Si des services sont utilisés, les informations de paiement sont obligatoires. L’enregistrement entraîne un paiement immédiat; en cas d’échec du paiement, vous devez envoyer une demande de nouveau paiement via l’interface modify."
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
          storeRegistModel.copy(bzAgencyId = payload.getClaim("agencyId").asString()), UUID.fromString(userUuid)
        )
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
      tags = listOf("Gestion des commerçants")
      summary = "Modifier un commerçant"
      description =
        "Modifie les informations du commerçant. Si les services utilisés changent: STANDBY = paiement immédiat, PENDING = paiement le 1er du mois suivant. Les services ne sont appliqués qu’après réussite du paiement."
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
          storeModifyModel.copy(bzAgencyId = payload.getClaim("agencyId").asString()), UUID.fromString(userUuid)
        )
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
      tags = listOf("Gestion des commerçants")
      summary = "Rechercher les jetons de carte par numéro d’entreprise"
      description = "Récupère les informations de carte à partir du numéro d’entreprise."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("businiessNo") {
          description = "Numéro d’entreprise"
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