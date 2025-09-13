package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceRegistModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceService
import io.allink.receipt.api.repository.TransactionUtil
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class StoreServiceImplModifyTest {

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
    mockkObject(TransactionUtil)
    coEvery { TransactionUtil.withTransaction(any<suspend () -> Any>()) } coAnswers {
      firstArg<suspend () -> Any>().invoke()
    }
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(TransactionUtil)
  }

  private fun servicesRegist(): List<NPointStoreServiceRegistModel> = listOf(
    NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 2000, rewardPoint = 300, serviceCommission = 100)
  )

  @Test
  fun `modifyStore - STANDBY 즉시결제 경로`() = runBlocking {
    val userUuid = UUID.randomUUID()
    val storeUid = UUID.randomUUID().toString()
    coEvery { storeRepository.update(any()) } returns 1

    // 기존 서비스 취소 후 재등록
    coEvery { nPointStoreServiceService.cancelNPointStoreServices(storeUid) } returns Unit
    coEvery { nPointStoreServiceService.registNPointStoreService(any(), any(), any(), any(), any()) } answers { firstArg() }

    // STANDBY: 즉시 결제일
    val storeBilling = StoreBillingRegistModel(
      tokenUuid = UUID.randomUUID(),
      billingAmount = 12_345,
      status = BillingStatusCode.STANDBY
    )
    // 결제 금액 검증 통과
    every { storeBillingService.calculAmount(any(), any()) } returns Triple(300, 12_345, 20_000)

    val billingSlot: CapturingSlot<StoreBillingModel> = slot()
    every { storeBillingService.initBillingModel(storeUid, any(), storeBilling, 12_345, any(), userUuid) } answers {
      StoreBillingModel(
        storeUid = storeUid,
        storeServiceSeq = secondArg(),
        tokenUuid = storeBilling.tokenUuid,
        status = BillingStatusCode.STANDBY,
        billingAmount = 12_345,
        regDate = LocalDateTime.now(),
        regBy = userUuid
      )
    }
    coEvery { storeBillingService.cancelBilling(storeUid) } returns 1
    coEvery { storeBillingService.updateBilling(capture(billingSlot)) } answers { billingSlot.captured.copy(id = 1L) }

    // NPointStore find/create
    coEvery { nPointStoreRepository.find(storeUid) } returns null
    coEvery { nPointStoreRepository.create(any()) } answers { firstArg() }

    // when
    service.modifyStore(
      StoreModifyModel(
        id = storeUid,
        storeName = "가맹점",
        businessNo = "123-45-67890",
        npointStoreServices = servicesRegist(),
        storeBilling = storeBilling
      ),
      userUuid
    )

    // then
    coVerify { nPointStoreServiceService.cancelNPointStoreServices(storeUid) }
    coVerify { storeBillingService.cancelBilling(storeUid) }
    coVerify { storeBillingService.updateBilling(any()) }
    coVerify { nPointStoreRepository.create(any()) }
  }

  @Test
  fun `modifyStore - PENDING 익월1일 결제 경로`() = runBlocking {
    val userUuid = UUID.randomUUID()
    val storeUid = UUID.randomUUID().toString()
    coEvery { storeRepository.update(any()) } returns 1

    coEvery { nPointStoreServiceService.cancelNPointStoreServices(storeUid) } returns Unit
    coEvery { nPointStoreServiceService.registNPointStoreService(any(), any(), any(), any(), any()) } answers { firstArg() }

    val storeBilling = StoreBillingRegistModel(
      tokenUuid = UUID.randomUUID(),
      billingAmount = 99_999,
      status = BillingStatusCode.PENDING
    )
    // 금액 검증 통과
    every { storeBillingService.calculAmount(any(), any()) } returns Triple(300, 99_999, 200_000)

    val initSlot: CapturingSlot<StoreBillingModel> = slot()
    every { storeBillingService.initBillingModel(storeUid, any(), storeBilling, 99_999, any(), userUuid) } answers {
      StoreBillingModel(
        storeUid = storeUid,
        storeServiceSeq = secondArg(),
        tokenUuid = storeBilling.tokenUuid,
        status = BillingStatusCode.PENDING,
        billingAmount = 99_999,
        regDate = LocalDateTime.now(),
        regBy = userUuid
      )
    }
    coEvery { storeBillingService.cancelBilling(storeUid) } returns 1
    coEvery { storeBillingService.updateBilling(capture(initSlot)) } answers { initSlot.captured.copy(id = 2L) }

    // 기존 NPointStore 존재
    coEvery { nPointStoreRepository.find(storeUid) } returns io.allink.receipt.api.domain.store.npoint.NPointStoreModel(
      id = storeUid,
      reservedPoints = 0,
      reviewPoints = 0,
      cumulativePoints = 0,
      regularPaymentAmounts = 0,
      status = StatusCode.PENDING,
      serviceStartAt = LocalDateTime.now(),
      regDate = LocalDateTime.now(),
      regBy = userUuid
    )

    // when
    service.modifyStore(
      StoreModifyModel(
        id = storeUid,
        storeName = "가맹점",
        businessNo = "123-45-67890",
        npointStoreServices = servicesRegist(),
        storeBilling = storeBilling
      ),
      userUuid
    )

    // then
    coVerify { nPointStoreServiceService.cancelNPointStoreServices(storeUid) }
    coVerify { storeBillingService.cancelBilling(storeUid) }
    coVerify { storeBillingService.updateBilling(any()) }
    // 기존이 있으므로 create는 호출되지 않을 수도 있음 (검증 생략)
  }
}