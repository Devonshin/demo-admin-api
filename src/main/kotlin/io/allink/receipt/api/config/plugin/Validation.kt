package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.Request
import io.allink.receipt.api.domain.agency.bz.BzAgencyModel
import io.allink.receipt.api.domain.login.VerificationCheckRequest
import io.allink.receipt.api.domain.login.VerificationCodeRequest
import io.allink.receipt.api.domain.store.StoreFilter
import io.allink.receipt.api.domain.store.StoreModifyModel
import io.allink.receipt.api.domain.store.StoreRegistModel
import io.allink.receipt.api.domain.store.StoreSearchFilter
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

    validate<Request<String>> { request ->
      if (request.data.isEmpty())
        ValidationResult.Invalid("Request.data should not be empty")
      else ValidationResult.Valid
    }

    validate<BzAgencyModel> { agency ->
      if (agency.id == null) ValidationResult.Invalid("Agency id is required")
      businessNoValidator(agency.businessNo)
    }

    validate<StoreFilter> { storeFilter ->
      businessNoValidator(storeFilter.businessNo)
    }

    validate<StoreSearchFilter> { storeFilter ->
      businessNoValidator(storeFilter.businessNo)
    }

    validate<StoreRegistModel> { storeRegistModel ->
      if (storeRegistModel.businessNo == null || storeRegistModel.businessNo.isEmpty()) {
        ValidationResult.Invalid("businessNo is required")
      } else if (storeRegistModel.storeName.isEmpty()) {
        ValidationResult.Invalid("storeName is required")
      } else ValidationResult.Valid
    }

    validate<StoreModifyModel> { storeModifyModel ->
      if (storeModifyModel.businessNo == null || storeModifyModel.businessNo.isEmpty()) {
        ValidationResult.Invalid("businessNo is required")
      } else if (storeModifyModel.id.isEmpty()) {
        ValidationResult.Invalid("id is required")
      } else if (storeModifyModel.storeName.isEmpty()) {
        ValidationResult.Invalid("storeName is required")
      } else ValidationResult.Valid
    }
  }
}

fun businessNoValidator(businessNo: String?): ValidationResult =
  if (businessNo != null && !isValidBusinessNo(businessNo)) {
    ValidationResult.Invalid("사업자 등록 번호가 유효하지 않습니다. ")
  } else ValidationResult.Valid

fun isValidBusinessNo(businessNo: String): Boolean {
  val businessNo = businessNo.replace("\\D", "")
  if (!Regex("\\d{10}").matches(businessNo)) return false
  val weights = listOf(1, 3, 7, 1, 3, 7, 1, 3, 5)
  var checkDigit = 0
  weights.forEachIndexed { index, weight ->
    val digit = businessNo[index].digitToInt()
    checkDigit += if (index == 8) {
      ((digit * weight) / 10) + ((digit * weight) % 10)
    } else {
      digit * weight
    }
  }

  checkDigit = (10 - (checkDigit % 10)) % 10

  return checkDigit == businessNo.last().digitToInt()
}
