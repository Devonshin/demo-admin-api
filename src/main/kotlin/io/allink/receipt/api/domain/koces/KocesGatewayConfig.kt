package io.allink.receipt.api.domain.koces

import io.allink.receipt.api.config.plugin.createHttpClient
import io.ktor.client.*
import io.ktor.server.config.*

/**
 * Payment Gateway Configuration
 * Created by: DevOps Team
 * Date: 2025-01-07
 */
class KocesGatewayConfig(config: ApplicationConfig) {

  val baseUrl: String = config.property("receiptGateKocesPay.baseUrl").getString()
  val paymentPath: String = config.property("receiptGateKocesPay.paymentPath").getString()
  val cancelPath: String = config.property("receiptGateKocesPay.cancelPath").getString()
  val timeoutSeconds: Long = config.property("receiptGateKocesPay.timeoutSeconds").getString().toLong()
  val maxRetries: Int = config.property("receiptGateKocesPay.maxRetries").getString().toInt()
  val token: String = config.property("receiptGateKocesPay.token").getString()

  fun createHttpClient(): HttpClient = createHttpClient(
    timeoutSeconds = timeoutSeconds,
    maxRetries = maxRetries,
    additionalHeaders = mapOf("X-Allink-Authorization" to "Bearer $token")
  )
}