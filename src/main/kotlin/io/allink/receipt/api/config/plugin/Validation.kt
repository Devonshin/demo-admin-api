package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.login.VerificationCheckRequest
import io.allink.receipt.api.domain.login.VerificationCodeRequest
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureValidation() {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  install(RequestValidation) {
    validate<VerificationCodeRequest> { codeRequest ->
      if (codeRequest.phone.isEmpty()) {
        ValidationResult.Invalid("Invalid phone is required")
      } else {
        ValidationResult.Valid
      }
    }

    validate<VerificationCheckRequest> { codeCheckRequest ->
      if (codeCheckRequest.verificationCode.isEmpty()) {
        ValidationResult.Invalid("Invalid verificationCode is required")
      } else if (codeCheckRequest.loginUuid.isEmpty()) {
        ValidationResult.Invalid("Invalid loginUuid is required")
      } else {
        ValidationResult.Valid
      }
    }

    validate<String> { bodyText ->
      if (!bodyText.startsWith("Hello"))
        ValidationResult.Invalid("Body text should start with 'Hello'")
      else ValidationResult.Valid
    }
  }
}
