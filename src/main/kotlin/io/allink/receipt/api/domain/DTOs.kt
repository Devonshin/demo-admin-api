package io.allink.receipt.api.domain

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 13/04/2025
 */

@Serializable
@Schema(description = "응답 래퍼", nullable = false)
data class Response<T>(val data: T)

@Serializable
@Schema(description = "에러 응답 객체", nullable = false, name = "ErrorResponse", title = "Error Response")
data class ErrorResponse(
  val code: String,
  val message: String
)
