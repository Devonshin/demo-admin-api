package io.allink.receipt.api.common

import io.allink.receipt.api.domain.*
import io.allink.receipt.api.domain.admin.MasterRole
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.code.ServiceCodeStatus
import io.allink.receipt.api.domain.login.Jwt
import io.allink.receipt.api.domain.login.VerificationCheckRequest
import io.allink.receipt.api.domain.login.VerificationCode
import io.allink.receipt.api.domain.login.VerificationCodeRequest
import io.allink.receipt.api.domain.npoint.NPointFilter
import io.allink.receipt.api.domain.npoint.NPointPayModel
import io.allink.receipt.api.domain.npoint.NPointUserModel
import io.allink.receipt.api.domain.receipt.*
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.user.*
import io.allink.receipt.api.domain.user.review.UserReviewStatus
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.time.LocalDateTime
import java.util.*

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
      value = Response(data = Jwt("jwt.payload.signature", "2022-01-01 00:00:00", "김대협", role = MasterRole()))
    }
  }
}

fun verificationCodeRequest(): SimpleBodyConfig.() -> Unit = {
  required = true
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
  required = true
  example("verification-request") {
    value = VerificationCheckRequest(
      loginUuid = "loginUuid", verificationCode = "123456"
    )
  }
}

fun userListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "목록 조회 요청"
  required = true
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
        data = PagedResult(
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

private val pointPayExample = NPointPayModel(
  id = 123,
  point = 500,
  status = "지급완료",
  store = SimpleStoreModel(
    id = "store-id",
    storeName = "김밥천국",
    franchiseCode = "FRANCHISE_CODE",
    businessNo = "1234567890",
    ceoName = "홍감자"
  ),
  user = NPointUserModel(
    id = "user-id",
    name = "정조준",
    phone = "0101221177",
    gender = "F",
    birthday = "19801114",
    nickname = "대장군",
  ),
  provideCase = "이벤트",
  pointTrNo = "fec68d91-fe2b-40a4-8bcb-4ec03ba05438",
  pointPayNo = "140629",
  regDate = LocalDateTime.now(),
)

fun pointListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "포인트 목록 조회 요청"
  required = true
  example("point-list-request") {
    value = NPointFilter(
      storeId = "123456-asdsa-aaasdsd-7890",
      businessNo = "1234567890",
      franchiseCode = "FRANCHISE_CODE",
      storeName = "김밥왕국",
      phone = "1234567890",
      userName = "전자용",
      userNickName = "돼지국밥",
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-03-17T12:00:00"),
        to = LocalDateTime.parse("2025-04-17T12:00:00"),
      ),
      page = Page(1, 10),
      sort = listOf(
        Sorter("field", "ASC")
      ),
    )
  }
}

fun pointListResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<NPointPayModel>>> {
    example("포인트 지급 목록 응답") {
      value = Response(
        data = PagedResult(
          items = listOf(pointPayExample),
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
    example("franchise-code-list-response") {
      value = listOf(
        ServiceCodeModel(
          id = "EDIYA",
          serviceGroup = "FRANCHISE",
          serviceName = "EDIYA",
          price = null,
          status = ServiceCodeStatus.ACTIVE,
          serviceType = "REVIEW"
        )
      )
    }
  }
}

fun bankCodeListResponse(): ResponseConfig.() -> Unit = {
  description = "은행 코드 목록 응답"
  body<Response<List<ServiceCodeModel>>> {
    example("bank-code-list-response") {
      value = listOf(
        ServiceCodeModel(
          id = "BANK-088",
          serviceGroup = "BANK_CODE",
          serviceName = "신한은행",
          price = null,
          status = ServiceCodeStatus.ACTIVE,
          serviceType = "REVIEW"
        )
      )
    }
  }
}

fun vendorCodeListResponse(): ResponseConfig.() -> Unit = {
  description = "밴더사 코드 목록 응답"
  body<Response<List<ServiceCodeModel>>> {
    example("vendor-code-list-response") {
      value = listOf(
        ServiceCodeModel(
          id = "VEN-KOCES",
          serviceGroup = "VEN_CODE",
          serviceName = "코세스",
          price = null,
          status = ServiceCodeStatus.ACTIVE,
          serviceType = "REVIEW"
        )
      )
    }
  }
}


fun issueReceiptListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "전자영수증 목록 조회 요청"
  required = true
  example("issue-receipt-list-request") {
    value = ReceiptFilter(
      storeId = "store-id",
      phone = "1234567890",
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-03-17T12:00:00"),
        to = LocalDateTime.parse("2025-04-17T12:00:00"),
      ),
      userName = "전자용",
      tagUid = "tag-uid",
      storeName = "김밥천국",
      userNickName = "돼지국밥",
      businessNo = "1234567890",
      franchiseCode = "FRANCHISE_CODE",
      page = Page(1, 10),
      sort = listOf(
        Sorter("field", "ASC")
      )
    )
  }
}


fun issueReceiptListResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<SimpleIssueReceiptModel>>> {
    example("전자영수증 목록 응답") {
      value = Response(
        data = PagedResult(
          items = listOf(
            SimpleIssueReceiptModel(
              id = "receipt-id",
              store = SimpleStoreModel(
                id = "store-id",
                storeName = "김밥천국",
                franchiseCode = "FRANCHISE_CODE",
                businessNo = "1234567890",
                ceoName = "강감찬"
              ),
              tagId = "tag-id",
              issueDate = LocalDateTime.now(),
              user = SimpleUserModel(
                id = "user-id",
                name = "정조준"
              ),
              receiptType = "PAYMENT",
              receiptAmount = 10000,
              originIssueId = "origin-issue-id"
            )
          ),
          totalPages = 1000,
          totalCount = 20000,
          currentPage = 1
        )
      )
    }
  }
}

fun issueReceiptDetailResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<IssueReceiptModel>> {
    example("전자영수증 상세 응답") {
      value = Response(
        IssueReceiptModel(
          id = "ed6843f8-67cd-454e-a843-f867cd454ee5",
          store = SimpleStoreModel(
            id = "ed6843f8-67cd-454e-a843-f867cd454ee1",
            storeName = "전자영수증 가맹점",
            franchiseCode = "EDIYA",
            businessNo = "123-12-12312",
            ceoName = "정관장"
          ),
          tag = SimpleMerchantTagReceiptModel(
            id = "E00123041234123",
            deviceId = "01",
          ),
          issueDate = LocalDateTime.now(),
          user = SimpleUserModel(
            id = "35f787d0-b983-4df8-b787-d0b9830df8ed",
            name = "나승소",
          ),
          receiptType = "PAYMENT",
          receiptAmount = 10000,
          originIssueId = "35f787d0-b983-4df8-b787-d0b9830df8ed",
          userPointReview = SimpleUserPointReviewModel(
            id = "35f787d0-b983-4df8-b787-d0b9830df8ed",
            status = UserReviewStatus.APPLIED
          ),
          edoc = SimpleEdocModel(
            id = "kakao",
            envelopId = "envelop-id-envelop-id-envelop-id",
            regDate = LocalDateTime.now(),
          ),
          advertisement = SimpleAdvertisementModel(
            id = UUID.randomUUID(),
            title = "불고기세트",
            merchantGroupId = "merchant-group-id",
          )
        )
      )
    }
  }
}