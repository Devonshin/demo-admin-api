package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.common.issueReceiptDetailResponse
import io.allink.receipt.api.common.issueReceiptListRequest
import io.allink.receipt.api.common.issueReceiptListResponse
import io.allink.receipt.api.domain.Response
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

fun Route.issueReceiptRoutes(
  issueReceiptService: IssueReceiptService
) {

  route("/receipts") {
    post("", {
      operationId = "issue-receipts"
      tags = listOf("Gestion des e-reçus")
      summary = "Consulter la liste des e‑reçus"
      description = "Récupère la liste des e‑reçus."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<ReceiptFilter>(issueReceiptListRequest())
      }
      response {
        code(HttpStatusCode.OK, issueReceiptListResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {

      val filter = call.receive<ReceiptFilter>()
      val findAllReceipt = issueReceiptService.findAllReceipt(filter)
      call.respond(
        HttpStatusCode.OK,
        Response(data = findAllReceipt)
      )
    }


    get("/detail/{userId}/{receiptId}", {
      operationId = "receipt-issue-detail"
      tags = listOf("Gestion des e-reçus")
      summary = "Consulter le détail de l’e‑reçu"
      description = "Récupère les informations détaillées de l’e‑reçu."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("userId") {
          description = "Identifiant utilisateur"
        }
        pathParameter<String>("receiptId") {
          description = "Identifiant unique du reçu"
        }
      }
      response {
        code(HttpStatusCode.OK, issueReceiptDetailResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val userId = call.request.pathVariables["userId"] ?: ""
      val receiptId = call.request.pathVariables["receiptId"] ?: ""

      call.respond(
        HttpStatusCode.OK,
        Response(
          data = issueReceiptService.findReceipt(userId, receiptId)
        )
      )
    }

  }
}