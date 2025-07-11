package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.koces.KocesGateResponse
import io.allink.receipt.api.domain.koces.KocesService
import io.allink.receipt.api.domain.store.npoint.NPointStoreRepository
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceRegistModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceService
import io.allink.receipt.api.exception.InvalidBillingStatusException
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.util.DateUtil
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 10/06/2025
 */

class StoreBillingServiceImpl(
  private val storeBillingRepository: StoreBillingRepository,
  private val nPointStoreRepository: NPointStoreRepository,
  private val nPointStoreServiceService: NPointStoreServiceService,
  private val kocesService: KocesService
) : StoreBillingService {

  private val logger = LoggerFactory.getLogger(StoreBillingServiceImpl::class.java)

  override suspend fun registBilling(billingModel: StoreBillingModel): StoreBillingModel =
    TransactionUtil.withTransaction {
      //결제 정보 추가
      storeBillingRepository.create(billingModel.copy(status = BillingStatusCode.STANDBY))
    }

  //매월 1일 정기 결제는 결제 데이터가 미리 생성되어 있지 않은 경우에만 생성한다.
  //가맹점의 이용 서비스가 수정 될 때 익월 결제 정보로 생성되므로 이 경우엔 자동으로 빌링 정보가 생성될 필요가 없다
  override suspend fun updateBilling(billingModel: StoreBillingModel): StoreBillingModel =
    TransactionUtil.withTransaction {
      storeBillingRepository.create(billingModel)
    }

  /*
  * 게이트 서버에 코세스 결제 대리 요청
  * */
  override suspend fun paymentStoreBilling(storeBillingModel: StoreBillingModel): StoreBillingModel =
    TransactionUtil.withTransaction {
      if (storeBillingModel.status == BillingStatusCode.STANDBY) {
        val response = kocesService.requestPayment(storeBillingModel.id!!) //결제 요청
        logger.info("Koces response: $response")
        val status = checkBillingStatus(response)
        val billingModel = storeBillingModel.copy(status = status)
        storeBillingRepository.update(billingModel)
        if (status == BillingStatusCode.COMPLETE) {
          activateNPointStore(storeBillingModel, billingModel.billingAmount!!)
        }
        return@withTransaction billingModel
      }
      storeBillingModel
    }

  private fun checkBillingStatus(response: KocesGateResponse): BillingStatusCode =
    if (response.resultCode == "OK" && response.resultMessage == "0000") {
      BillingStatusCode.COMPLETE
    } else {
      BillingStatusCode.FAIL
    }

  private suspend fun activateNPointStore(
    storeBillingModel: StoreBillingModel,
    billingAmount: Int
  ) {
    val nPointStore = nPointStoreRepository.find(storeBillingModel.storeUid) ?: throw Exception("No NStore found")
    val npointServices = nPointStoreServiceService.getStoreServices(storeBillingModel.storeUid).map {
      NPointStoreServiceRegistModel(
        serviceCode = it.service?.id!!,
        serviceCharge = it.serviceCharge,
        rewardDeposit = it.rewardDeposit,
        rewardPoint = it.rewardPoint,
        serviceCommission = it.serviceCommission
      )
    }
    val rewardPointAmount = calculRewardPointAmount(npointServices)
    logger.info("결제 완료 nPointStore: $nPointStore")
    nPointStoreRepository.update(
      nPointStore.copy(
        status = StatusCode.ACTIVE,
        reviewPoints = rewardPointAmount,
        reservedPoints = nPointStore.reservedPoints?.plus(billingAmount),
        cumulativePoints = nPointStore.cumulativePoints.plus(billingAmount),
        modBy = storeBillingModel.regBy,
        modDate = DateUtil.nowLocalDateTime()
      )
    )
  }

  override suspend fun cancelBilling(storeUid: String): Int = TransactionUtil.withTransaction {
    storeBillingRepository.cancelBilling(storeUid)
  }


  override fun initBillingModel(
    storeUid: String,
    storeServiceSeq: Int,
    storeBilling: StoreBillingRegistModel,
    totalAmount: Int,
    now: LocalDateTime,
    userUuid: UUID
  ): StoreBillingModel {

    storeBilling.status.let {
      if (it != BillingStatusCode.PENDING && it != BillingStatusCode.STANDBY) {
        throw InvalidBillingStatusException("결제 정보 등록 시 상태 값은 PENDING, STANDBY 만 가능합니다. 전송된 값 : $it")
      }
    }

    return StoreBillingModel(
      storeUid = storeUid,
      storeServiceSeq = storeServiceSeq,
      tokenUuid = storeBilling.tokenUuid,
      status = storeBilling.status,
      billingAmount = totalAmount,
      bankCode = storeBilling.bankCode,
      bankAccountNo = storeBilling.bankAccountNo,
      bankAccountName = storeBilling.bankAccountName,
      regDate = now,
      regBy = userUuid
    )
  }

  override fun calculAmount(
    registeredServices: List<NPointStoreServiceRegistModel>,
    now: LocalDateTime
  ): Triple<Int, Int, Int> {
    val lengthOfMonth = YearMonth.now().lengthOfMonth()
    var totalAmount = 0
    var rewardPoint = 0
    for (registeredService in registeredServices) {
      logger.info("registered service[${registeredService.serviceCode}] - serviceCharge: ${registeredService.serviceCharge}, rewardDeposit: ${registeredService.rewardDeposit}, rewardPoint: ${registeredService.rewardPoint}")
      totalAmount += registeredService.serviceCharge!! + registeredService.rewardDeposit!!
      rewardPoint += registeredService.rewardPoint!!
    }
    val toDayTotalAmount = calculate(totalAmount, lengthOfMonth, lengthOfMonth - now.dayOfMonth + 1)
    logger.info("totalAmount: $totalAmount, toDayTotalAmount: $toDayTotalAmount, rewardPoint: $rewardPoint")
    return Triple(rewardPoint, toDayTotalAmount, totalAmount)
  }

  //now 를 기준으로 말일까지 남은 일수로 계산
  fun calculRewardPointAmount(
    registeredServices: List<NPointStoreServiceRegistModel>
  ): Int {
    var rewardPoint = 0
    for (registeredService in registeredServices) {
      rewardPoint += registeredService.rewardPoint!!
    }
    return rewardPoint
  }

  private fun calculate(totalAmount: Int, lengthOfMonth: Int, remainingDays: Int): Int {
    return (totalAmount.toLong() * remainingDays / lengthOfMonth).toInt()
  }
}