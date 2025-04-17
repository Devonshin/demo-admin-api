package io.allink.receipt.api.domain.sns

/**
 * Package: io.allink.receipt.admin.domain.sns
 * Created: Devonshin
 * Date: 13/04/2025
 */

interface VerificationService {

  suspend fun sendVerificationMessage(to: String, message: String, expired: String)

}