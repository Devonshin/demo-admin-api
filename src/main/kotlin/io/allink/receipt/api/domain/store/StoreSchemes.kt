package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.*
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.time.LocalDateTime

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


fun storeRegisterRequest(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<StoreModel>> {
    example("가맹점 등록 요청 데이터") {
      description = "가맹점 상세 등록 요청 데이터"
      value = Response(
        data = storeExample
      )
    }
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
  status = StoreStatus.NORMAL,
  partnerLoginId = null,
  partnerLoginPassword = ""
)