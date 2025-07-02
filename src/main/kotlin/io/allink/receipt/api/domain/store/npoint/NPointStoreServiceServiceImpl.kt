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

  override suspend fun cancelNPointStoreServices(storeUid: String) = TransactionUtil.withTransaction {
    nPointStoreServiceRepository.cancelAllStoreService(storeUid)
    logger.info("Canceled all services for storeUid: $storeUid")
  }

  //이용 서비스 등록
  override suspend fun registNPointStoreReviewService(
    merchantSelectedServices: List<NPointStoreServiceModifyModel>,
    storeUid: String,
    userUuid: UUID,
    yyMMddHHmm: String,
    now: LocalDateTime
  ): List<NPointStoreServiceModifyModel> = TransactionUtil.withTransaction {
    cancelNPointStoreServices(storeUid)
    val merchantSelectedServices = merchantSelectedServices.associateBy {
      it.serviceCode
    }
    val services = getServiceCodes()
    val validatedSelectedReviewServices = validateSelectedService(merchantSelectedServices, services)
    merchantSelectedServices[DLVRVIEWPT]?.let { service ->
      validatedSelectedReviewServices.add(initDefService(services[DLVRVIEWPT]!!))
    }
    merchantSelectedServices[CPNADVTZ]?.let { service ->
      validatedSelectedReviewServices.add(initDefService(services[CPNADVTZ]!!))
    }
    for (reviewServiceModel in validatedSelectedReviewServices) {
      nPointStoreServiceRepository.create(
        NPointStoreServiceModel(
          id = NPointStoreServiceId(
            storeServiceSeq = yyMMddHHmm,
            storeUid = storeUid,
            serviceCode = reviewServiceModel.serviceCode,
          ),
          serviceCharge = reviewServiceModel.serviceCharge!!,
          rewardDeposit = reviewServiceModel.rewardDeposit,
          rewardPoint = reviewServiceModel.rewardPoint,
          serviceCommission = reviewServiceModel.serviceCommission,
          status = StatusCode.ACTIVE,
          regDate = now,
          regBy = userUuid
        )
      )
    }
    validatedSelectedReviewServices
  }

  private fun initDefService(
    service: ServiceCodeModel
  ): NPointStoreServiceModifyModel = NPointStoreServiceModifyModel(
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
    merchantSelectedServices: Map<String, NPointStoreServiceModifyModel>,
    services: Map<String, ServiceCodeModel>
  ): MutableList<NPointStoreServiceModifyModel> {
    val eReceiptServiceCode = services[ERECEIPT]!!
    merchantSelectedServices[REVIEWPRJ]?.let { merchantService ->
      val reviewProjectServiceCode = services[REVIEWPRJ]!!
      validateDepositAndCommissionAmount(merchantService, reviewProjectServiceCode)
      return mutableListOf(
        merchantService.copy(
          serviceCode = ERECEIPT,
          serviceCharge = eReceiptServiceCode.price,
          rewardDeposit = 0,
          rewardPoint = 0,
          serviceCommission = 0,
          status = StatusCode.ACTIVE,
        ),
        merchantService.copy(
          serviceCode = REVIEWPRJ,
          serviceCharge = reviewProjectServiceCode.price,
          status = StatusCode.ACTIVE,
        )
      )
    }
    merchantSelectedServices[REVIEWPT]?.let { merchantService ->
      val reviewServiceCode = services[REVIEWPT]!!
      validateDepositAndCommissionAmount(merchantService, reviewServiceCode)
      return mutableListOf(
        merchantService.copy(
          serviceCode = ERECEIPT,
          serviceCharge = eReceiptServiceCode.price,
          rewardDeposit = 0,
          rewardPoint = 0,
          serviceCommission = 0,
          status = StatusCode.ACTIVE,
        ),
        merchantService.copy(
          serviceCode = REVIEWPT,
          serviceCharge = reviewServiceCode.price,
          status = StatusCode.ACTIVE,
        )
      )
    }
    merchantSelectedServices[ERECEIPT]?.let { merchantService ->
      return mutableListOf(
        merchantService
      )
    }
    return mutableListOf()
  }

  fun validateDepositAndCommissionAmount(selectedService: NPointStoreServiceModifyModel, serviceCodeModel: ServiceCodeModel) {
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