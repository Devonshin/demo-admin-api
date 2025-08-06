package io.allink.receipt.api.domain.koces

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.LoggerFactory
import java.util.*

/**
 * External Payment Service implementation
 * Integrates with e-receipt-gate payment API
 * Created by: DevOps Team
 * Date: 2025-01-07
 */
class KocesServiceImpl(
  private val httpClient: HttpClient,
  private val config: KocesGatewayConfig
) : KocesService {

  private val logger = LoggerFactory.getLogger(KocesServiceImpl::class.java)

  override suspend fun requestPayment(billingSeq: Long): KocesGateResponse {
    logger.info("Requesting payment for billingSeq: $billingSeq")

    try {
      val response = httpClient.post("${config.baseUrl}${config.paymentPath}") {
        contentType(ContentType.Application.Json)
        setBody(KocesPayRequest(billingSeq = billingSeq))
      }
      val kocesResponse: KocesGateResponse = response.body()
      logger.info("Payment request successful for billingSeq: $billingSeq, response: ${response.bodyAsText()}, kocesResponse : $kocesResponse")
      return kocesResponse
    } catch (e: TimeoutCancellationException) {
      logger.error("Payment request timeout for billingSeq: $billingSeq", e)
      return KocesGateResponse(
        resultCode = "NOTOK",
        errorMessage = "Payment request timeout for billingSeq: $billingSeq",
        errorCode = "TIMEOUT",
      )
    } catch (e: Exception) {
      logger.error("Payment request failed for billingSeq: $billingSeq", e)
      return KocesGateResponse(
        resultCode = "NOTOK",
        errorMessage = "Payment request failed for billingSeq: $billingSeq",
        errorCode = "ERROR",
      )
    }
  }

  override suspend fun cancelPayment(requestSeq: Long, tokenUuid: UUID): KocesGateResponse {
    logger.info("Cancelling payment for requestSeq: $requestSeq")

    try {
      val response = httpClient.post("${config.baseUrl}${config.cancelPath}") {
        contentType(ContentType.Application.Json)
        setBody(
          KocesCancelRequest(
            requestSeq = requestSeq,
            tokenUuid = tokenUuid
          )
        )
      }
      logger.info("Cancel request successful for requestSeq: $requestSeq, tokenUuid: $tokenUuid, response: ${response.bodyAsText()}")
      val kocesResponse: KocesGateResponse = response.body()

      return kocesResponse
    } catch (e: TimeoutCancellationException) {
      logger.error("Payment cancel timeout for requestSeq: $requestSeq", e)
      return KocesGateResponse(
        resultCode = "NOTOK",
        errorMessage = "Payment cancel timeout for requestSeq: $requestSeq",
        errorCode = "TIMEOUT",
      )
    } catch (e: Exception) {
      logger.error("Payment cancel failed for requestSeq: $requestSeq", e)
      return KocesGateResponse(
        resultCode = "NOTOK",
        errorMessage = "Payment cancel failed for requestSeq: $requestSeq",
        errorCode = "ERROR",
      )
    }
  }
}
