package io.allink.receipt.api.config.plugin

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime
import java.util.*


/**
 * Package: io.allink.receipt.api.config.plugin
 * Created: Devonshin
 * Date: 08/07/2025
 */

fun createHttpClient(
  timeoutSeconds: Long = 30,
  maxRetries: Int = 3,
  additionalHeaders: Map<String, String> = emptyMap()
): HttpClient = HttpClient(CIO) {

  install(ContentNegotiation) {
    json(Json {
      serializersModule = SerializersModule {
        contextual(UUID::class, UUIDSerializer)
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
      }
      prettyPrint = false
      ignoreUnknownKeys = true
      encodeDefaults = true
    })
  }

  install(HttpTimeout) {
    requestTimeoutMillis = timeoutSeconds * 1000
    connectTimeoutMillis = minOf(timeoutSeconds * 1000, 10_000)
    socketTimeoutMillis = timeoutSeconds * 1000
  }

  install(HttpRequestRetry) {
    retryOnServerErrors(maxRetries = maxRetries)
    exponentialDelay()
  }

  defaultRequest {
    headers.append("Content-Type", "application/json")
    headers.append("Accept", "application/json")
    additionalHeaders.forEach { (key, value) ->
      headers.append(key, value)
    }
  }
}
