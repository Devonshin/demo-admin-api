package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.Request
import io.allink.receipt.api.domain.agency.bz.BzAgencyModel
import io.allink.receipt.api.domain.login.VerificationCheckRequest
import io.allink.receipt.api.domain.login.VerificationCodeRequest
import io.allink.receipt.api.domain.store.StoreFilter
import io.allink.receipt.api.domain.store.StoreModifyModel
import io.allink.receipt.api.domain.store.StoreRegistModel
import io.allink.receipt.api.domain.store.StoreSearchFilter
import io.allink.receipt.api.domain.agency.bz.BzAgencyFilter
import io.allink.receipt.api.util.isValidBusinessNo
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

    validate<Request<String>> { request ->
      if (request.data.isEmpty())
        ValidationResult.Invalid("Request.data should not be empty")
      else ValidationResult.Valid
    }

    validate<BzAgencyModel> { agency ->
      if (agency.id == null) ValidationResult.Invalid("Agency id is required")
      else businessNoValidator(agency.businessNo)
    }

    // BzAgencyFilter는 검색 필터이므로 체크섬 검증이 아닌 "형식"만 검증합니다.
    validate<BzAgencyFilter> { filter ->
      businessNoFormatValidator(filter.businessNo)
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
    ValidationResult.Invalid("사업자 등록 번호[$businessNo]가 유효하지 않습니다. 예)123-45-67890")
  } else ValidationResult.Valid

private val BUSINESS_NO_FORMAT_REGEX = Regex("""^\d{3}-\d{2}-\d{5}$""")

fun businessNoFormatValidator(businessNo: String?): ValidationResult =
  if (businessNo != null && !BUSINESS_NO_FORMAT_REGEX.matches(businessNo)) {
    ValidationResult.Invalid("사업자 등록 번호[$businessNo] 형식이 올바르지 않습니다. 예)123-45-67890")
  } else ValidationResult.Valid

