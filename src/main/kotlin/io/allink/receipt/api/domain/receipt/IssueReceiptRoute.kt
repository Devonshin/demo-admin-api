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
      tags = listOf("전자영수증 관리")
      summary = "전자영수증 목록 조회"
      description = "전자영수증 목록을 조회합니다."
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
      tags = listOf("전자영수증 관리")
      summary = "전자영수증 상세 조회"
      description = "전자영수증 상세 정보를 조회합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("userId") {
          description = "사용자 id"
        }
        pathParameter<String>("receiptId") {
          description = "영수증 고유 id"
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