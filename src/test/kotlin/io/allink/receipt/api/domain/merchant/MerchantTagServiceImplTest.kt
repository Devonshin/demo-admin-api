package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.store.StoreService
import io.allink.receipt.api.repository.TransactionUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import java.util.*

/**
 * @file MerchantTagServiceImplTest.kt
 * @brief merchant 도메인의 Service 단위 테스트 - 트랜잭션 유틸 모킹 + 외부 의존(DynamoDB/StoreService) 모킹
 * @author Devonshin
 * @date 2025-09-12
 */
class MerchantTagServiceImplTest {

  private val merchantTagRepository: MerchantTagRepository = mockk(relaxed = true)
  private val storeService: StoreService = mockk(relaxed = true)
  private val dynamo: DynamoDbClient = mockk(relaxed = true)

  private val service: MerchantTagService = MerchantTagServiceImpl(
    merchantTagRepository, storeService, dynamo
  )

  @Test
  fun `should_get_tags`() {
    // given
    val filter = MerchantTagFilter(
      id = null, name = "", storeId = null, businessNo = null, storeName = null, franchiseCode = null,
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = java.time.LocalDateTime.parse("2025-01-01T00:00:00"),
        to = java.time.LocalDateTime.parse("2025-12-31T23:59:59")
      )
    )
    coEvery { merchantTagRepository.findAll(filter) } returns PagedResult(emptyList(), 0, 1, 0)

    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction<PagedResult<SimpleMerchantTagModel>>(any()) } coAnswers {
      val block = arg<suspend () -> PagedResult<SimpleMerchantTagModel>>(0)
      block.invoke()
    }

    // when
    val result = kotlinx.coroutines.runBlocking { service.getTags(filter) }

    // then
    assertEquals(0, result.totalCount)
    coVerify { merchantTagRepository.findAll(filter) }
  }

  @Test
  fun `should_modify_tag`() {
    // given
    val now = io.allink.receipt.api.util.DateUtil.nowLocalDateTime()
    val modify = MerchantTagModifyModel(id = "T-100", name = "새 태그", storeId = "S-1", deviceId = "D-9")
    val store = io.allink.receipt.api.domain.store.StoreModel(id = "S-1", storeName = "가맹점", businessNo = "111")

    coEvery { merchantTagRepository.findForUpdate("T-100") } returns null
    coEvery { merchantTagRepository.create(any()) } answers { firstArg() }
    coEvery { storeService.findStore("S-1") } returns store
    coEvery { merchantTagRepository.find("T-100") } returns MerchantTagModel(
      id = "T-100",
      store = io.allink.receipt.api.domain.merchant.SimpleMerchantStoreDetailModel(id = "S-1", storeName = "가맹점", deleteDate = null),
      merchantGroupId = store.franchiseCode,
      merchantStoreId = store.id,
      tagName = "새 태그",
      deviceId = "D-9",
      storeUid = store.id,
      regDate = now,
      modDate = null,
      regBy = java.util.UUID.randomUUID(),
      modBy = null
    )

    // DynamoDB: query는 빈결과로 모킹
    every { dynamo.query(any<software.amazon.awssdk.services.dynamodb.model.QueryRequest>()) } returns QueryResponse.builder().items(emptyList()).build()

    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction<MerchantTagModel>(any()) } coAnswers {
      val block = arg<suspend () -> MerchantTagModel>(0)
      block.invoke()
    }

    // when
    val result = kotlinx.coroutines.runBlocking { service.modifyTag(modify, UUID.randomUUID()) }

    // then
    assertEquals("새 태그", result.tagName)
    coVerify { merchantTagRepository.create(any()) }
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(TransactionUtil)
  }
}
