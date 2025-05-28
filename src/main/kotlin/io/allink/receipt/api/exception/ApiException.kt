package io.allink.receipt.api.exception

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 14/04/2025
 */

sealed class ApiException(override val message: String, val code: String) : RuntimeException(message)

class NotFoundUserException(message: String): ApiException(message, "NOT_FOUND_USER")

class InvalidVerificationCodeException(message: String): ApiException(message, "INVALID_VERIFICATION_CODE")

class InvalidFileUploadException(message: String): ApiException(message, "INVALID_FILE_UPLOAD")