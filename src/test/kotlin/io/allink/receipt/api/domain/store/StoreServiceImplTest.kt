package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.store.npoint.NPointStoreModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceRegistModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceService
import io.allink.receipt.api.repository.TransactionUtil
import io.ktor.server.plugins.*
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class StoreServiceImplTest {

  private val storeRepository: StoreRepository = mockk(relaxed = true)
  private val nPointStoreRepository: io.allink.receipt.api.domain.store.npoint.NPointStoreRepository = mockk(relaxed = true)
  private val nPointStoreServiceService: NPointStoreServiceService = mockk(relaxed = true)
  private val storeBillingService: StoreBillingService = mockk(relaxed = true)
  private val storeBillingTokenRepository: StoreBillingTokenRepository = mockk(relaxed = true)

  private val service: StoreService = StoreServiceImpl(
    storeRepository,
    nPointStoreRepository,
    nPointStoreServiceService,
    storeBillingService,
    storeBillingTokenRepository
  )

  @BeforeEach
  fun setUp() {
    // TransactionUtil.withTransaction 블록을 실제 트랜잭션 없이 바로 실행하도록 목킹
    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction(any<suspend () -> Any>()) } coAnswers {
      firstArg<suspend () -> Any>().invoke()
    }
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(TransactionUtil)
  }

  @Test
  fun `registStore - services 선택 및 금액 일치 시 가맹점과 결제, NPoint 관련 레코드가 정상 생성되고 storeUid 반환`() = runBlocking {
    // given
    val userUuid = UUID.randomUUID()
    val regist = StoreRegistModel(
      storeName = "테스트가맹점",
      businessNo = "123-45-67890",
      ceoName = "홍길동",
      email = "owner@test.com",
      npointStoreServices = listOf(
        NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 2000, rewardPoint = 300, serviceCommission = 100)
      ),
      storeBilling = StoreBillingRegistModel(
        tokenUuid = UUID.randomUUID(),
        billingAmount = 10_000,
        status = BillingStatusCode.STANDBY
      )
    )

    coEvery { storeRepository.findByNameAndBzNo(regist.storeName, regist.businessNo) } returns null

    val createdStoreSlot: CapturingSlot<StoreModel> = slot()
    coEvery { storeRepository.create(capture(createdStoreSlot)) } answers { createdStoreSlot.captured }

    // nPoint 서비스 등록 시 계산된 결과
    coEvery { nPointStoreServiceService.registNPointStoreService(any(), any(), any(), any(), any()) } answers {
      firstArg()
    }
    every { storeBillingService.calculAmount(any(), any()) } returns Triple(300, 10_000, 20_000)

    val initBilling = StoreBillingModel(
      storeUid = "temp",
      storeServiceSeq = 1,
      tokenUuid = regist.storeBilling!!.tokenUuid,
      status = BillingStatusCode.STANDBY,
      billingAmount = 10_000,
      regDate = LocalDateTime.now(),
      regBy = userUuid
    )
    every {
      storeBillingService.initBillingModel(any(), any(), regist.storeBilling!!, 10_000, any(), userUuid)
    } returns initBilling
    coEvery { storeBillingService.registBilling(initBilling) } returns initBilling.copy(id = 1L)

    // NPointStore 미존재 -> 생성 경로
    coEvery { nPointStoreRepository.find(any()) } returns null
    coEvery { nPointStoreRepository.create(any()) } answers { firstArg() }

    // when
    val resultUid = service.registStore(regist, userUuid)

    // then
    assertNotNull(resultUid)
    // create에 담긴 StoreModel의 id와 반환된 id가 동일해야 함
    val createdId = createdStoreSlot.captured.id
    assertEquals(createdId, resultUid)

    coVerify(exactly = 1) { storeRepository.create(any()) }
    coVerify(exactly = 1) { nPointStoreServiceService.registNPointStoreService(any(), any(), any(), any(), any()) }
    coVerify(exactly = 1) { storeBillingService.registBilling(any()) }
    coVerify { nPointStoreRepository.create(any()) }
  }

  @Test
  fun `registStore - 서비스 목록이 빈 경우 결제 검증 없이 storeUid 반환`() = runBlocking {
    // given
    val userUuid = UUID.randomUUID()
    val regist = StoreRegistModel(
      storeName = "빈서비스",
      businessNo = "123-45-67890",
      npointStoreServices = emptyList(),
      // storeBilling 없이도 정상 동작 기대(분기 상 let 내부에서 early return)
      storeBilling = null
    )
    coEvery { storeRepository.findByNameAndBzNo(regist.storeName, regist.businessNo) } returns null

    val createdStoreSlot: CapturingSlot<StoreModel> = slot()
    coEvery { storeRepository.create(capture(createdStoreSlot)) } answers { createdStoreSlot.captured }

    // when
    val resultUid = service.registStore(regist, userUuid)

    // then
    assertNotNull(resultUid)
    assertEquals(createdStoreSlot.captured.id, resultUid)
    // 결제 관련 호출이 없어야 함
    coVerify(exactly = 0) { storeBillingService.registBilling(any()) }
    coVerify(exactly = 0) { nPointStoreServiceService.registNPointStoreService(any(), any(), any(), any(), any()) }
  }

  @Test
  fun `registStore - 결제 금액 불일치 시 BadRequestException`() = runBlocking {
    // given
    val userUuid = UUID.randomUUID()
    val regist = StoreRegistModel(
      storeName = "금액불일치",
      businessNo = "123-45-67890",
      npointStoreServices = listOf(
        NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 2000, rewardPoint = 300, serviceCommission = 100)
      ),
      storeBilling = StoreBillingRegistModel(
        tokenUuid = UUID.randomUUID(),
        billingAmount = 9_999,
        status = BillingStatusCode.STANDBY
      )
    )
    coEvery { storeRepository.findByNameAndBzNo(regist.storeName, regist.businessNo) } returns null
    val createdStoreSlot: CapturingSlot<StoreModel> = slot()
    coEvery { storeRepository.create(capture(createdStoreSlot)) } answers { createdStoreSlot.captured }

    coEvery { nPointStoreServiceService.registNPointStoreService(any(), any(), any(), any(), any()) } answers { firstArg() }
    every { storeBillingService.calculAmount(any(), any()) } returns Triple(300, 10_000, 20_000)

    // when / then
    assertThrows(BadRequestException::class.java) {
      runBlocking { service.registStore(regist, userUuid) }
    }
  }

  @Test
  fun `modifyStore - 서비스 비어있으면 모든 NPoint 서비스 취소 및 NPointStore 상태 DELETED 업데이트`() = runBlocking {
    // given
    val userUuid = UUID.randomUUID()
    val storeUid = UUID.randomUUID().toString()
    val modify = StoreModifyModel(
      id = storeUid,
      storeName = "수정대상",
      businessNo = "123-45-67890",
      npointStoreServices = emptyList(),
      storeBilling = null
    )

    coEvery { storeRepository.update(any()) } returns 1
    // NPointStore 존재하여 상태 업데이트 경로로
    val existingNPointStore = NPointStoreModel(
      id = storeUid,
      reservedPoints = 0,
      reviewPoints = 0,
      cumulativePoints = 0,
      regularPaymentAmounts = 0,
      status = StatusCode.ACTIVE,
      serviceStartAt = LocalDateTime.now(),
      regDate = LocalDateTime.now(),
      regBy = userUuid
    )
    coEvery { nPointStoreRepository.find(storeUid) } returns existingNPointStore
    coEvery { nPointStoreRepository.update(any()) } returns 1

    // when
    service.modifyStore(modify, userUuid)

    // then
    coVerify { nPointStoreServiceService.cancelNPointStoreServices(storeUid) }
    coVerify { nPointStoreRepository.update(withArg { updated ->
      assertEquals(StatusCode.DELETED, updated.status)
      assertEquals(userUuid, updated.modBy)
    }) }
  }

  @Test
  fun `findStore - 가맹점 조회 시 npoint 서비스와 결제 토큰 목록 포함`() = runBlocking {
    // given
    val storeUid = UUID.randomUUID().toString()
    val businessNo = "123-45-67890"
    val baseStore = StoreModel(
      id = storeUid,
      storeName = "상점",
      businessNo = businessNo,
      regDate = LocalDateTime.now(),
      regBy = UUID.randomUUID()
    )
    coEvery { storeRepository.find(storeUid) } returns baseStore

    val services = listOf(
      io.allink.receipt.api.domain.store.npoint.NPointStoreServiceModel(
        id = io.allink.receipt.api.domain.store.npoint.NPointStoreServiceId(1, storeUid, "REVIEWPT"),
        service = null,
        serviceCharge = 1000,
        rewardDeposit = 2000,
        rewardPoint = 300,
        serviceCommission = 100,
        status = StatusCode.PENDING,
        regDate = LocalDateTime.now(),
        regBy = UUID.randomUUID()
      )
    )
    coEvery { nPointStoreServiceService.getStoreServices(storeUid) } returns services

    val tokens = listOf(
      StoreBillingTokenModel(
        id = UUID.randomUUID(),
        businessNo = businessNo,
        token = "tok",
        tokenInfo = "info",
        status = StatusCode.ACTIVE,
        regDate = LocalDateTime.now(),
        regBy = UUID.randomUUID()
      )
    )
    coEvery { storeBillingTokenRepository.findAllByBusinessNo(businessNo) } returns tokens

    // when
    val store = service.findStore(storeUid)

    // then
    assertNotNull(store)
    assertEquals(services, store!!.npointStoreServices)
    assertEquals(tokens, store.storeBillingTokens)
  }
}
