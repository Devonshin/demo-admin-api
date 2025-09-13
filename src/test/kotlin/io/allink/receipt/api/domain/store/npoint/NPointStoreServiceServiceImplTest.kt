package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.code.ServiceCodeRepository
import io.allink.receipt.api.repository.TransactionUtil
import io.ktor.server.plugins.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.runBlocking

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class NPointStoreServiceServiceImplTest {

  private val nPointStoreServiceRepository: NPointStoreServiceRepository = mockk(relaxed = true)
  private val serviceCodeRepository: ServiceCodeRepository = mockk(relaxed = true)

  private val service = NPointStoreServiceServiceImpl(
    nPointStoreServiceRepository,
    serviceCodeRepository
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
  fun `getStoreServices - 최신 키의 값 반환, 없으면 빈 리스트`() {
    // given
    val storeUid = "store-1"
    val m1 = listOf(
      NPointStoreServiceModel(
        id = NPointStoreServiceId(1, storeUid, "ERECEIPT"),
        service = null,
        serviceCharge = 0,
        status = StatusCode.PENDING,
        regDate = LocalDateTime.now(),
        regBy = UUID.randomUUID()
      )
    )
    val m2 = listOf(
      NPointStoreServiceModel(
        id = NPointStoreServiceId(2, storeUid, "REVIEWPT"),
        service = null,
        serviceCharge = 1000,
        status = StatusCode.PENDING,
        regDate = LocalDateTime.now(),
        regBy = UUID.randomUUID()
      )
    )
    coEvery { nPointStoreServiceRepository.findAllStoreService(storeUid) } returns mapOf(1 to m1, 2 to m2)

    // when
    val latest = runBlocking { service.getStoreServices(storeUid) }

    // then
    assertEquals(m2, latest)

    // and when empty
    coEvery { nPointStoreServiceRepository.findAllStoreService(storeUid) } returns emptyMap()
    val empty = runBlocking { service.getStoreServices(storeUid) }
    assertEquals(emptyList<NPointStoreServiceModel>(), empty)
  }

  @Test
  fun `cancelNPointStoreServices - 저장소 취소 호출`() = runBlocking {
    // given
    val storeUid = "store-1"
    coEvery { nPointStoreServiceRepository.cancelAllStoreService(storeUid) } returns 1

    // when
    service.cancelNPointStoreServices(storeUid)

    // then
    coVerify { nPointStoreServiceRepository.cancelAllStoreService(storeUid) }
  }

  @Test
  fun `calculate - 수수료 및 보증금 경계값 검증`() {
    // REVIEWPT
    assertEquals(1000, service.calculateReviewCommission(2_000))
    assertEquals(1500, service.calculateReviewCommission(3_000))
    assertEquals(5_000, service.calculateReviewCommission(11_000))

    assertEquals(200_000, service.calculateReviewDeposit(2_000))
    assertEquals(300_000, service.calculateReviewDeposit(3_000))
    assertEquals(1_000_000, service.calculateReviewDeposit(11_000))

    // REVIEWPRJ deposit
    assertEquals(200_000, service.calculateReviewProjectDeposit(2_000))
    assertEquals(300_000, service.calculateReviewProjectDeposit(3_000))
    assertEquals(500_000, service.calculateReviewProjectDeposit(11_000))
  }

  @Test
  fun `validateSelectedService - REVIEWPT와 REVIEWPRJ 케이스`() {
    val services = mapOf(
      "ERECEIPT" to ServiceCodeModel(id = "ERECEIPT", serviceGroup = "MERT_SVC", serviceName = "전자영수증", price = 0, status = null, serviceType = null),
      "REVIEWPT" to ServiceCodeModel(id = "REVIEWPT", serviceGroup = "MERT_SVC", serviceName = "리뷰 리워드", price = 1000, status = null, serviceType = null),
      "REVIEWPRJ" to ServiceCodeModel(id = "REVIEWPRJ", serviceGroup = "MERT_SVC", serviceName = "999+리뷰", price = 500, status = null, serviceType = null)
    )

    // REVIEWPT 선택 시 ERECEIPT + REVIEWPT 2건 구성
    val sel1 = mapOf(
      "REVIEWPT" to NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 200_000, rewardPoint = 2000, serviceCommission = 1000)
    )
    val out1 = service.validateSelectedService(sel1, services)
    assertEquals(2, out1.size)

    // REVIEWPRJ 선택 시 ERECEIPT(기본료 0) + REVIEWPRJ 2건 구성
    val sel2 = mapOf(
      "REVIEWPRJ" to NPointStoreServiceRegistModel(serviceCode = "REVIEWPRJ", serviceCharge = 500, rewardDeposit = 200_000, rewardPoint = 2000, serviceCommission = 500)
    )
    val out2 = service.validateSelectedService(sel2, services)
    assertEquals(2, out2.size)
  }

  @Test
  fun `validateDepositAndCommissionAmount - 금액 불일치시 예외`() {
    val scReviewPt = ServiceCodeModel(id = "REVIEWPT", serviceGroup = "MERT_SVC", serviceName = "리뷰 리워드", price = 1000, status = null, serviceType = null)

    // 기본료 불일치
    assertThrows(BadRequestException::class.java) {
      service.validateDepositAndCommissionAmount(
        NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 999, rewardDeposit = 200_000, rewardPoint = 2000, serviceCommission = 1000),
        scReviewPt
      )
    }

    // 보증금 불일치
    assertThrows(BadRequestException::class.java) {
      service.validateDepositAndCommissionAmount(
        NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 10, rewardPoint = 2000, serviceCommission = 1000),
        scReviewPt
      )
    }

    // 수수료 불일치
    assertThrows(BadRequestException::class.java) {
      service.validateDepositAndCommissionAmount(
        NPointStoreServiceRegistModel(serviceCode = "REVIEWPT", serviceCharge = 1000, rewardDeposit = 200_000, rewardPoint = 2000, serviceCommission = 999),
        scReviewPt
      )
    }
  }
}