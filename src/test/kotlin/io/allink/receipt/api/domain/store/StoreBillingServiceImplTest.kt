package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.koces.KocesGateResponse
import io.allink.receipt.api.domain.koces.KocesService
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceRegistModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceService
import io.allink.receipt.api.exception.InvalidBillingStatusException
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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class StoreBillingServiceImplTest {

  private val storeBillingRepository: StoreBillingRepository = mockk(relaxed = true)
  private val nPointStoreRepository: io.allink.receipt.api.domain.store.npoint.NPointStoreRepository = mockk(relaxed = true)
  private val nPointStoreServiceService: NPointStoreServiceService = mockk(relaxed = true)
  private val kocesService: KocesService = mockk(relaxed = true)

  private val service = StoreBillingServiceImpl(
    storeBillingRepository,
    nPointStoreRepository,
    nPointStoreServiceService,
    kocesService
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

  @Test
  fun `initBillingModel - 유효하지 않은 상태면 예외`() {
    val now = LocalDateTime.now()
    val invalid = StoreBillingRegistModel(
      tokenUuid = UUID.randomUUID(),
      billingAmount = 0,
      status = BillingStatusCode.COMPLETE
    )
    assertThrows(InvalidBillingStatusException::class.java) {
      service.initBillingModel("store", 1, invalid, 1000, now, UUID.randomUUID())
    }
  }

  @Test
  fun `paymentStoreBilling - STANDBY OK 응답시 COMPLETE로 업데이트 및 NPointStore 활성화`() = runBlocking {
    val userUuid = UUID.randomUUID()
    val now = LocalDateTime.now()
    val model = StoreBillingModel(
      id = 1L,
      storeUid = "store-1",
      storeServiceSeq = 10,
      tokenUuid = UUID.randomUUID(),
      status = BillingStatusCode.STANDBY,
      billingAmount = 10_000,
      regDate = now,
      regBy = userUuid
    )

    // 외부 결제 성공
    coEvery { kocesService.requestPayment(1L) } returns KocesGateResponse(resultCode = "OK", resultMessage = "0000", resultData = null, errorMessage = null, errorCode = null)
    coEvery { storeBillingRepository.update(any()) } returns 1

    // NPoint 활성화 경로에 필요한 데이터
    coEvery { nPointStoreRepository.find("store-1") } returns io.allink.receipt.api.domain.store.npoint.NPointStoreModel(
      id = "store-1",
      reservedPoints = 0,
      reviewPoints = 0,
      cumulativePoints = 0,
      regularPaymentAmounts = 0,
      status = StatusCode.PENDING,
      serviceStartAt = now,
      regDate = now,
      regBy = userUuid
    )
    val svc = NPointStoreServiceModel(
      id = io.allink.receipt.api.domain.store.npoint.NPointStoreServiceId(10, "store-1", "REVIEWPT"),
      service = ServiceCodeModel(id = "REVIEWPT", serviceGroup = "MERT_SVC", serviceName = "리뷰", price = 1000, status = null, serviceType = null),
      serviceCharge = 1000,
      rewardDeposit = 2000,
      rewardPoint = 300,
      serviceCommission = 100,
      status = StatusCode.PENDING,
      regDate = now,
      regBy = userUuid
    )
    coEvery { nPointStoreServiceService.getStoreServices("store-1") } returns listOf(svc)
    coEvery { nPointStoreRepository.update(any()) } returns 1

    // when
    val updated = service.paymentStoreBilling(model)

    // then
    assertEquals(BillingStatusCode.COMPLETE, updated.status)
    coVerify { storeBillingRepository.update(any()) }
    coVerify { nPointStoreRepository.update(any()) }
  }

  @Test
  fun `paymentStoreBilling - STANDBY 실패 응답시 FAIL로 업데이트`() = runBlocking {
    val userUuid = UUID.randomUUID()
    val now = LocalDateTime.now()
    val model = StoreBillingModel(
      id = 1L,
      storeUid = "store-1",
      storeServiceSeq = 10,
      tokenUuid = UUID.randomUUID(),
      status = BillingStatusCode.STANDBY,
      billingAmount = 10_000,
      regDate = now,
      regBy = userUuid
    )

    coEvery { kocesService.requestPayment(1L) } returns KocesGateResponse(resultCode = "NOTOK", resultMessage = "1234", resultData = null, errorMessage = null, errorCode = null)
    coEvery { storeBillingRepository.update(any()) } returns 1

    val updated = service.paymentStoreBilling(model)
    assertEquals(BillingStatusCode.FAIL, updated.status)
  }

  @Test
  fun `cancelBilling - 저장소 위임`() = runBlocking {
    coEvery { storeBillingRepository.cancelBilling("store-1") } returns 1
    val result = service.cancelBilling("store-1")
    assertEquals(1, result)
  }

  @Test
  fun `calculRewardPointAmount - 등록된 서비스 포인트 합`() {
    val list = listOf(
      NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 2000, rewardPoint = 300, serviceCommission = 100),
      NPointStoreServiceRegistModel(serviceCode = "ERECEIPT", serviceCharge = 0, rewardDeposit = 0, rewardPoint = 0, serviceCommission = 0)
    )
    assertEquals(300, service.calculRewardPointAmount(list))
  }
}