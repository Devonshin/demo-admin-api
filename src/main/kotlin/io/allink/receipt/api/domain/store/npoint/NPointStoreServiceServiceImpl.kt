package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.common.Constant.Companion.CPNADVTZ
import io.allink.receipt.api.common.Constant.Companion.DLVRVIEWPT
import io.allink.receipt.api.common.Constant.Companion.ERECEIPT
import io.allink.receipt.api.common.Constant.Companion.MERCHANT_SERVICE_GROUP_CODE
import io.allink.receipt.api.common.Constant.Companion.REVIEWPRJ
import io.allink.receipt.api.common.Constant.Companion.REVIEWPT
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.code.ServiceCodeRepository
import io.allink.receipt.api.repository.TransactionUtil
import io.ktor.server.plugins.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import kotlin.math.min

/**
 * Package: io.allink.receipt.api.domain.store.npoint
 * Created: Devonshin
 * Date: 10/06/2025
 */

class NPointStoreServiceServiceImpl(
  val nPointStoreServiceRepository: NPointStoreServiceRepository,
  val serviceCodeRepository: ServiceCodeRepository,
) : NPointStoreServiceService {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  override suspend fun getStoreServices(storeUid: String): List<NPointStoreServiceModel> {
    return nPointStoreServiceRepository.findAllStoreService(storeUid)
      .maxByOrNull { it.key }
      ?.value
      ?: emptyList()
  }

  //활성화가 아닌 상태만 무효처리한다
  override suspend fun cancelNPointStoreServices(storeUid: String) = TransactionUtil.withTransaction {
    nPointStoreServiceRepository.cancelAllStoreService(storeUid)
    logger.info("Canceled npoint services for storeUid: $storeUid")
  }

  //이용 서비스 등록
  override suspend fun registNPointStoreService(
    services: List<NPointStoreServiceRegistModel>,
    storeUid: String,
    userUuid: UUID,
    storeServiceSeq: Int,
    now: LocalDateTime
  ): List<NPointStoreServiceRegistModel> = TransactionUtil.withTransaction {
    val selectedServices = services.associateBy {
      it.serviceCode
    }
    val services = getServiceCodes()
    val validatedSelectedReviewServices = validateSelectedService(selectedServices, services)
    selectedServices[DLVRVIEWPT]?.let { service ->
      validatedSelectedReviewServices.add(initDefService(services[DLVRVIEWPT]!!))
    }
    selectedServices[CPNADVTZ]?.let { service ->
      validatedSelectedReviewServices.add(initDefService(services[CPNADVTZ]!!))
    }
    for (reviewServiceModel in validatedSelectedReviewServices) {
      nPointStoreServiceRepository.create(
        NPointStoreServiceModel(
          id = NPointStoreServiceId(
            storeServiceSeq = storeServiceSeq,
            storeUid = storeUid,
            serviceCode = reviewServiceModel.serviceCode,
          ),
          serviceCharge = reviewServiceModel.serviceCharge!!,
          rewardDeposit = reviewServiceModel.rewardDeposit,
          rewardPoint = reviewServiceModel.rewardPoint,
          serviceCommission = reviewServiceModel.serviceCommission,
          status = StatusCode.PENDING, //최초엔 대기 상태로, 이후 결제가 완료되면 ACTIVE로 변경한다
          regDate = now,
          regBy = userUuid
        )
      )
    }
    validatedSelectedReviewServices
  }

  private fun initDefService(
    service: ServiceCodeModel
  ): NPointStoreServiceRegistModel = NPointStoreServiceRegistModel(
    serviceCode = service.id!!,
    serviceCharge = 0,
    rewardDeposit = 0,
    rewardPoint = 0,
    serviceCommission = service.price,
  )

  override suspend fun getServiceCodes(): Map<String, ServiceCodeModel> = TransactionUtil.withTransaction {
    val services = serviceCodeRepository.findAll(MERCHANT_SERVICE_GROUP_CODE).associateBy {
      it.id!!
    }
    services
  }

  override suspend fun registService(
    merchantSelectedService: NPointStoreServiceModel
  ): NPointStoreServiceModel {
    return nPointStoreServiceRepository.create(merchantSelectedService)
  }

  fun validateSelectedService(
    selectedServices: Map<String, NPointStoreServiceRegistModel>,
    services: Map<String, ServiceCodeModel>
  ): MutableList<NPointStoreServiceRegistModel> {
    val eReceiptServiceCode = services[ERECEIPT]!!
    selectedServices[REVIEWPRJ]?.let { selectedService ->
      val serviceCodeModel = services[REVIEWPRJ]!!
      validateDepositAndCommissionAmount(selectedService, serviceCodeModel)
      return mutableListOf(
        selectedService.copy(
          serviceCode = ERECEIPT,
          serviceCharge = 0/*eReceiptServiceCode.price*/,
          rewardDeposit = 0,
          rewardPoint = 0,
          serviceCommission = 0,
        ),
        selectedService.copy(
          serviceCode = REVIEWPRJ,
          serviceCharge = serviceCodeModel.price,
        )
      )
    }
    selectedServices[REVIEWPT]?.let { selectedService ->
      val serviceCodeModel = services[REVIEWPT]!!
      validateDepositAndCommissionAmount(selectedService, serviceCodeModel)
      return mutableListOf(
        selectedService.copy(
          serviceCode = ERECEIPT,
          serviceCharge = eReceiptServiceCode.price,
          rewardDeposit = 0,
          rewardPoint = 0,
          serviceCommission = 0,
        ),
        selectedService.copy(
          serviceCode = REVIEWPT,
          serviceCharge = serviceCodeModel.price,
        )
      )
    }
    selectedServices[ERECEIPT]?.let { merchantService ->
      return mutableListOf(
        merchantService
      )
    }
    return mutableListOf()
  }

  fun validateDepositAndCommissionAmount(selectedService: NPointStoreServiceRegistModel, serviceCodeModel: ServiceCodeModel) {
    val rewardDeposit = selectedService.rewardDeposit
    val rewardCommission = selectedService.serviceCommission
    val serviceCode = selectedService.serviceCode
    val serviceCharge = selectedService.serviceCharge
    val rewardPoint = selectedService.rewardPoint ?: 0

    logger.info("selectedService: $selectedService")

    if (serviceCharge!! != serviceCodeModel.price) {
      throw BadRequestException("[${serviceCodeModel.serviceName}] 리워드 기본료 금액이 올바르지 않습니다. serviceCharge: $serviceCharge shouldBe: ${serviceCodeModel.price}")
    }
    when (serviceCode) {
      REVIEWPT -> {
        val calculatedDeposit = calculateReviewDeposit(rewardPoint)
        if (rewardDeposit != calculatedDeposit) {
          logger.error("rewardDeposit[${rewardDeposit}] != depositValue:[$calculatedDeposit]")
          throw BadRequestException("[${serviceCodeModel.serviceName}] 보증금 금액이 올바르지 않습니다. rewardDeposit: $rewardDeposit shouldBe: $calculatedDeposit")
        }
        val calculatedRewardCommission = calculateReviewCommission(rewardPoint)
        if (rewardCommission != calculatedRewardCommission) {
          logger.error("rewardCommission[${rewardCommission}] != calculatedRewardCommission:[$calculatedRewardCommission]")
          throw BadRequestException("[${serviceCodeModel.serviceName}] 수수료 금액이 올바르지 않습니다. rewardCommission: $rewardCommission shouldBe: $calculatedRewardCommission")
        }
      }

      REVIEWPRJ -> {
        val calculatedReviewCommission = 500
        if (rewardCommission != calculatedReviewCommission) {
          logger.error("rewardCommission[${rewardCommission}] != 500")
          throw BadRequestException("[${serviceCodeModel.serviceName}] 수수료 금액이 올바르지 않습니다. rewardCommission: $rewardCommission shouldBe: $calculatedReviewCommission")
        }
        val calculatedRewardDeposit = calculateReviewProjectDeposit(rewardPoint)
        if (rewardDeposit != calculatedRewardDeposit) {
          logger.error("rewardDeposit[${rewardDeposit}] != calculatedRewardDeposit[${calculatedRewardDeposit}]")
          throw BadRequestException("[${serviceCodeModel.serviceName}] 보증금 금액이 올바르지 않습니다. rewardDeposit: $rewardDeposit shouldBe: $calculatedRewardDeposit")
        }
      }
    }
  }

  fun calculateReviewCommission(rewardPoints: Int): Int {
    return if (rewardPoints <= 2_000) {
      1_000
    } else if (rewardPoints > 10_000) {
      5_000
    } else {
      (rewardPoints * .5).toInt()
    }
  }

  fun calculateReviewDeposit(rewardPoints: Int): Int {
    return if (rewardPoints <= 2_000) {
      200_000
    } else if (rewardPoints <= 10_000) {
      rewardPoints * 100
    } else {
      1_000_000
    }
  }

  fun calculateReviewProjectDeposit(rewardPoints: Int): Int {
    return if (rewardPoints <= 2_000) {
      200_000
    } else if (rewardPoints <= 10_000) {
      min(rewardPoints * 100, 500_000)
    } else {
      500_000
    }
  }


}