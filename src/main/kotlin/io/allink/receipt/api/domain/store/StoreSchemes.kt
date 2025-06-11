package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.*
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.code.ServiceCodeStatus
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceId
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceModifyModel
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 27/05/2025
 */


fun storeListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "가맹점 목록 조회 요청"
  example("store-list-request") {
    value = StoreFilter(
      name = "store-name",
      id = "store-id",
      businessNo = "1234567890",
      franchiseCode = "FRANCHISE_CODE",
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-03-17T12:00:00"),
        to = LocalDateTime.parse("2025-04-17T12:00:00"),
      ),
      page = Page(1, 10),
      sort = listOf(
        Sorter("field", "ASC")
      )
    )
  }
}

fun storeSearchRequest(): SimpleBodyConfig.() -> Unit = {
  description = "매핑 용 가맹점 목록 조회 요청"
  example("store-search-request") {
    value = StoreSearchFilter(
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


fun storeSearchResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<StoreSearchModel>>> {
    example("가맹점 목록 응답") {
      value = Response(
        data = PagedResult(
          items = listOf(storeSearchExample),
          totalPages = 1000,
          totalCount = 20000,
          currentPage = 1
        )
      )
    }
  }
}

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


fun storeRegisterRequest(): SimpleBodyConfig.() -> Unit = {
  description = "가맹점 등록/수정 요청 데이터"
  example("store-regist-request") {
    value = StoreRegistModel(
      storeName = "매장명",
      businessNo = "123-45-67890",
      franchiseCode = "FRANCHISE_1",
      addr1 = "서울시 서대문구 동대문",
      addr2 = "3층",
      tel = "021032001",
      mobile = "01012341234",
      managerName = "나원참",
      workType = "receipt-print",
      ceoName = "저것참",
      businessType = "1231212345",
      eventType = "퉁신판매업",
      email = "kkk22@nav.com",
      businessNoLaw = null,
      status = StatusCode.NORMAL,
      npointStoreServices = listOf(
        NPointStoreServiceModifyModel(
          serviceCode = "REVIEWPRJ",
          serviceCharge = 50_000,
          rewardDeposit = 300_000,
          rewardPoint = 500,
          serviceCommission = 500,
          status = StatusCode.ACTIVE
        ),
        NPointStoreServiceModifyModel(
          serviceCode = "DLVRVIEWPT",
          serviceCharge = 0,
          rewardDeposit = 0,
          rewardPoint = 500,
          serviceCommission = 500,
          status = StatusCode.ACTIVE
        )
      ),
      couponAdYn = false,
      storeBilling = StoreBillingRegistModel(
        tokenUuid = "611b2187-b838-46a1-9b21-87b838b6a17a",
        billingAmount = 5000,
        bankCode = "001",
        bankAccountNo = "1231-23123-123",
        bankAccountName = "홍길동"
      ),
    )
  }
}

fun storeModifyRequest(): SimpleBodyConfig.() -> Unit = {
  description = "가맹점 수정 요청 데이터"
  example("store-modify-request") {
    value = StoreModifyModel(
      id = "store-123",
      storeName = "매장명",
      businessNo = "123-45-67890",
      franchiseCode = "FRANCHISE_1",
      addr1 = "서울시 서대문구 동대문",
      addr2 = "3층",
      tel = "021032001",
      mobile = "01012341234",
      managerName = "나원참",
      workType = "receipt-print",
      ceoName = "저것참",
      businessType = "1231212345",
      eventType = "퉁신판매업",
      email = "kkk22@nav.com",
      businessNoLaw = null,
      status = StatusCode.NORMAL,
      npointStoreServices = listOf(
        NPointStoreServiceModifyModel(
          serviceCode = "REVIEWPRJ",
          serviceCharge = 50_000,
          rewardDeposit = 300_000,
          rewardPoint = 500,
          serviceCommission = 500,
          status = StatusCode.ACTIVE
        ),
        NPointStoreServiceModifyModel(
          serviceCode = "DLVRVIEWPT",
          serviceCharge = 0,
          rewardDeposit = 0,
          rewardPoint = 500,
          serviceCommission = 500,
          status = StatusCode.ACTIVE
        )
      ),
      couponAdYn = false,
      storeBilling = StoreBillingRegistModel(
        tokenUuid = "611b2187-b838-46a1-9b21-87b838b6a17a",
        billingAmount = 5000,
        bankCode = "001",
        bankAccountNo = "1231-23123-123",
        bankAccountName = "홍길동"
      ),
    )
  }
}

private val storeSearchExample = StoreSearchModel(
  id = "store-123",
  storeName = "매장명",
  businessNo = "123-45-67890",
  franchiseCode = "FRANCHISE_1",
  ceoName = "저것참",
  tel = "021032001",
  businessType = "서비스업",
  eventType = "통신판매업",
  deviceType = "OKPOS"
)

private val storeExample = StoreModel(
  id = "store-123",
  storeName = "매장명",
  businessNo = "123-45-67890",
  franchiseCode = "FRANCHISE_1",
  storeType = "가맹점 타입",
  zoneCode = "1234123124",
  addr1 = "서울시 서대문구 동대문",
  addr2 = "3층",
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
  iconUrl = null,
  logoUrl = "https://logourl.com",
  receiptWidthInch = null,
  status = StatusCode.NORMAL,
  partnerLoginId = null,
  regDate = LocalDateTime.now(),
  modDate = LocalDateTime.now(),
  regBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
  modBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
  npointStoreServices = listOf(
    NPointStoreServiceModel(
      id = NPointStoreServiceId(
        storeServiceSeq = "2506101100",
        storeUid = "store-123",
        serviceCode = "DLVRVIEWPT",
      ),
      service = ServiceCodeModel(
        id = "REVIEWPRJ",
        serviceGroup = "MERT_SVC",
        serviceName = "네이버 999+리뷰",
        price = 500_000,
        status = ServiceCodeStatus.ACTIVE,
        serviceType = "REVIEW"
      ),
      serviceCharge = 50_000,
      rewardDeposit = 300_000,
      rewardPoint = 500,
      serviceCommission = 500,
      status = StatusCode.ACTIVE,
      regDate = LocalDateTime.now(),
      modDate = LocalDateTime.now(),
      regBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
      modBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
    ),
    NPointStoreServiceModel(
      id = NPointStoreServiceId(
        storeServiceSeq = "2506101100",
        storeUid = "store-123",
        serviceCode = "DLVRVIEWPT",
      ),
      service = ServiceCodeModel(
        id = "REVIEWPT",
        serviceGroup = "MERT_SVC",
        serviceName = "네이버 리뷰 리워드",
        price = 30_000,
        status = ServiceCodeStatus.INACTIVE,
        serviceType = "REVIEW"
      ),
      serviceCharge = 0,
      rewardDeposit = 0,
      rewardPoint = 500,
      serviceCommission = 500,
      status = StatusCode.ACTIVE,
      regDate = LocalDateTime.now(),
      modDate = LocalDateTime.now(),
      regBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
      modBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
    )
  ),
  couponAdYn = false,
  storeBilling = StoreBillingModel(
    storeUid = "store-123",
    tokenUuid = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
    billingAmount = 5_000,
    bankCode = "001",
    bankAccountNo = "1231-23123-123",
    bankAccountName = "홍길동",
    regDate = LocalDateTime.now(),
    regBy = UUID.fromString("611b2187-b838-46a1-9b21-87b838b6a17a"),
    id = 123u,
    storeServiceSeq = "2506101100",
    status = BillingStatusCode.COMPLETE
  ),
)