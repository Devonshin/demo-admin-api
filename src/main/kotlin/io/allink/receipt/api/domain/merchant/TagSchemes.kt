package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.domain.store.StoreStatus
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 27/05/2025
 */

fun tagListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "태그 목록 조회 요청"
  required = true
  example("tag-list-request") {
    value = MerchantTagFilter(
      id = "E00TEST1234",
      name = "태그명",
      storeId = "123456-asdsa-aaasdsd-7890",
      businessNo = "1234567890",
      franchiseCode = "FRANCHISE_CODE",
      storeName = "김밥왕국",
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

fun tagModifyRequest(): SimpleBodyConfig.() -> Unit = {
  description = "태그 등록/수정 요청"
  required = true
  example("tag-modify-request") {
    value = MerchantTagModifyModel(
      id = "E00TEST1234",
      name = "태그명",
      storeId = "123456-asdsa-aaasdsd-7890",
      deviceId = ""
    )
  }
}

fun tagDetailResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<MerchantTagModel>> {
    example("태그 상세 정보 응답") {
      value = Response(
        data = tagExample
      )
    }
  }
}

fun tagListResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<SimpleMerchantTagModel>>> {
    example("가맹점 목록 응답") {
      value = Response(
        data = PagedResult(
          items = listOf(simpleTagExample),
          totalPages = 1000,
          totalCount = 20000,
          currentPage = 1
        )
      )
    }
  }
}

private val tagExample = MerchantTagModel(
  id = "TAGE00123",
  store = SimpleMerchantStoreDetailModel(
    id = "store-123",
    storeName = "매장명",
    businessNo = "123-45-67890",
    franchiseCode = "FRANCHISE_1",
    regDate = LocalDateTime.now(),
    deviceType = "CAT",
    deleteDate = null,
    ceoName = "저것참",
    tel = "02-1234-1234",
    businessType = "1231212345",
    eventType = "퉁신판매업",
    modDate = LocalDateTime.now(),
    status = StoreStatus.NORMAL,
  ),
  tagName = "영수증 태그",
  merchantGroupId = "uuid-like-group-id",
  deviceId = "229",
  storeUid = "uuid-like-store-123",
  regDate = LocalDateTime.parse("2025-03-17T12:00:00"),
  modDate = null,
)

private val simpleTagExample = SimpleMerchantTagModel(
  id = "TAGE00123",
  store = SimpleMerchantTagStoreModel(
    id = "uuid-like-merchant-store-123",
    storeName = "이디야별다방",
    franchiseCode = "EDIYA",
    businessNo = "1231212312",
    status = StoreStatus.NORMAL,
  ),
  regDate = LocalDateTime.parse("2025-03-17T12:00:00"),
  modDate = null,
)
