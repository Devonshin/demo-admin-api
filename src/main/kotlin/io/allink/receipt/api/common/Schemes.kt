package io.allink.receipt.api.common

import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.login.Jwt
import io.allink.receipt.api.domain.login.VerificationCheckRequest
import io.allink.receipt.api.domain.login.VerificationCode
import io.allink.receipt.api.domain.login.VerificationCodeRequest
import io.allink.receipt.api.domain.store.StoreFilter
import io.allink.receipt.api.domain.store.StoreModel
import io.allink.receipt.api.domain.user.*
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 15/04/2025
 */


fun errorResponse(): ResponseConfig.() -> Unit = {
  description = "실패 응답"
  body<Response<ErrorResponse>> {
    example("실패 응답") {
      value = Response(
        ErrorResponse(
          message = "Bad Request", code = "400"
        )
      )
    }
  }
}

fun verificationCodeCheckResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<Jwt>> {
    example("성공 응답") {
      description = "API 이용에 사용할 JWT"
      value = Response(data = Jwt("jwt.payload.signature", "2022-01-01 00:00:00"))
    }
  }
}

fun verificationCodeRequest(): SimpleBodyConfig.() -> Unit = {
  example("phone") {
    value = VerificationCodeRequest("01012345678")
  }
}

fun verificationCodeResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<VerificationCode>>() {
    example("성공 응답") {
      value = Response(
        VerificationCode(
          loginUuid = "loginUuid", expireDate = "2022-01-01 00:00:00"
        )
      )
    }
  }
}

fun verificationCodeCheckRequest(): SimpleBodyConfig.() -> Unit = {
  description = "인증코드 확인 요청"
  example("verification-request") {
    value = VerificationCheckRequest(
      loginUuid = "loginUuid", verificationCode = "123456"
    )
  }
}

fun userListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "목록 조회 요청"
  example("user-list-request") {
    value = UserFilter(
      phone = "1234567890",
      nickName = "test-nickname",
      name = "test-name",
      gender = "M",
      age = Age(from = "1999", to = "2001"),
      page = Page(1, 10),
      sort = listOf(
        Sorter("field", "ASC")
      )
    )
  }
}


fun userListResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<UserModel>>>() {
    example("사용자 목록 응답") {
      value = Response(
        data = PagedResult<UserModel>(
          items = listOf(exampleUser),
          totalPages = 1000,
          totalCount = 20000,
          currentPage = 1
        )
      )
    }
  }
}

private val exampleUser = UserModel(
  id = "c7f0d23e-eceb-4434-b489-668c0b61a7f9",
  name = "장정종",
  status = UserStatus.NORMAL,
  phone = "0101221177",
  gender = "F",
  ci = "***",
  birthday = "19801114",
  localYn = "Y",
  email = "<EMAIL>",
  role = UserRole.USER,
  joinSocialType = "KAKAO",
  nickname = "대장군",
  mtchgId = "1234567890",
  cpointRegType = "kakao",
  cpointRegDate = LocalDateTime.now(),
  regDate = LocalDateTime.now(),
  modDate = LocalDateTime.now(),
)

fun userDetailResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<UserModel>> {
    example("사용자 상세 데이터 응답") {
      description = "사용자 상세 데이터"
      value = Response(
        data = exampleUser
      )
    }
  }
}

private val storeExample = StoreModel(
  id = "store-123",
  storeName = "매장명",
  businessNo = "123-45-67890",
  franchiseCode = "FRANCHISE_1",
  storeType = "가맹점 타입",
  zoneCode = "1234123124",
  addr1 = "서울시 서대문구 동대문",
  addr2 = "3층",
  regDate = LocalDateTime.now(),
  deleteDate = null,
  mapUrl = null,
  lat = "123.14213123",
  lon = "123.123412312",
  tel = "021032001",
  mobile = "01012341234",
  managerName = "나원참",
  siteLink = null,
  workType = "receipt-print",
  ceoName = "저것참",
  businessType = "1231212345",
  eventType = "퉁신판매업",
  email = "kkk22@nav.com",
  businessNoLaw = null,
  modDate = LocalDateTime.now(),
  iconUrl = null,
  logoUrl = "https://logourl.com",
  receiptWidthInch = null,
  partnerLoginId = null,
  partnerLoginPassword = ""
)

fun storeDetailResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<StoreModel>> {
    example("가맹점 상세 데이터 응답") {
      description = "가맹점 상세 데이터"
      value = Response(
        data = storeExample
      )
    }
  }
}


fun storeListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "가맹점 목록 조회 요청"
  example("store-list-request") {
    value = StoreFilter(
      name = "store-name",
      id = "store-id",
      businessNo = "1234567890",
      franchiseCode = "FRANCHISE_CODE",
      page = Page(1, 10),
      sort = listOf(
        Sorter("field", "ASC")
      )
    )
  }
}

fun storeListResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<StoreModel>>> {
    example("가맹점 목록 응답") {
      value = Response(
        data = PagedResult(
          items = listOf(storeExample),
          totalPages = 1000,
          totalCount = 20000,
          currentPage = 1
        )
      )
    }
  }
}


fun franchiseCodeListResponse(): ResponseConfig.() -> Unit = {
  description = "프랜차이즈 코드 목록 응답"
  body<Response<List<ServiceCodeModel>>> {
    example("franchise-code-list-reponse") {
      value = listOf(
        ServiceCodeModel(
          id = "EDIYA", serviceGroup = "FRANCHISE", serviceName = "EDIYA", price = null, status = "ACTIVE"
        )
      )
    }
  }
}

