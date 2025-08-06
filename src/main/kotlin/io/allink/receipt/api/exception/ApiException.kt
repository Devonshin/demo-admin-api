package io.allink.receipt.api.exception

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 14/04/2025
 */

sealed class ApiException(override val message: String, val code: String) : RuntimeException(message)

class NotFoundUserException(message: String) : ApiException(message, "NOT_FOUND_USER")
class NotFoundStoreException(message: String) : ApiException(message, "NOT_FOUND_STORE")
class InvalidVerificationCodeException(message: String) : ApiException(message, "INVALID_VERIFICATION_CODE")
class InvalidFileUploadException(message: String) : ApiException(message, "INVALID_FILE_UPLOAD")
class PaymentRequestException(message: String?) : ApiException("결제 요청 중 오류가 발생했습니다: $message", "PAYMENT_REQUEST_ERROR")
class PaymentTimeoutException() : ApiException("결제 요청 시간이 초과되었습니다", "PAYMENT_TIMEOUT")
class PaymentCancelException(message: String?) : ApiException("결제 취소 중 오류가 발생했습니다: $message", "PAYMENT_CANCEL_ERROR")
class InvalidBillingStatusException(message: String) : ApiException(message, "INVALID_BILLING_STATUS")