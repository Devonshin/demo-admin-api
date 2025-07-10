package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.koces.KocesService
import io.allink.receipt.api.domain.store.npoint.NPointStoreRepository
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.util.DateUtil
import org.slf4j.LoggerFactory

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 10/06/2025
 */

class StoreBillingServiceImpl(
  private val storeBillingRepository: StoreBillingRepository,
  private val nPointStoreRepository: NPointStoreRepository,
  private val kocesService: KocesService
) : StoreBillingService {

  private val logger = LoggerFactory.getLogger(StoreBillingServiceImpl::class.java)

  override suspend fun registBilling(billingModel: StoreBillingModel): StoreBillingModel = TransactionUtil.withTransaction {
    //결제 정보 추가
    storeBillingRepository.create(billingModel.copy(status = BillingStatusCode.PENDING))
  }

  //todo 매월 1일 정기 결제
  //매월 1일 정기 결제는 결제 데이터가 미리 생성되어 있지 않은 경우에만 생성한다.
  //가맹점의 이용 서비스가 수정 될 때 익월 결제 정보로 생성되므로 이 경우엔 자동으로 빌링 정보가 생성될 필요가 없다
  override suspend fun updateBilling(billingModel: StoreBillingModel): StoreBillingModel = TransactionUtil.withTransaction {
    //이전에 등록된, 아직 결제 완료가 되지 않은 건들은 모두 취소처리
    storeBillingRepository.cancelBilling(billingModel.storeUid)
    //결제 정보 추가, 익월 결제 예정건으로 추가
    storeBillingRepository.create(billingModel)
  }

  override suspend fun paymentStoreBilling(storeBillingModel: StoreBillingModel): StoreBillingModel = TransactionUtil.withTransaction {
    if(storeBillingModel.status == BillingStatusCode.PENDING) {
      val response = kocesService.requestPayment(storeBillingModel.id!!)
      logger.info("Koces response: $response")
      val status = if (response.resultCode == "OK" && response.resultMessage == "0000") {
        BillingStatusCode.COMPLETE
      } else {
        BillingStatusCode.FAIL
      }
      val billingModel = storeBillingModel.copy(
        status = status
      )
      storeBillingRepository.update(billingModel)
      if(status == BillingStatusCode.COMPLETE) {
        val nPointStore = nPointStoreRepository.find(storeBillingModel.storeUid) ?: throw Exception("No store found")
        nPointStore.copy(
          status = StatusCode.ACTIVE,
          reservedPoints = nPointStore.reservedPoints?.plus(billingModel.billingAmount!!),
          cumulativePoints = nPointStore.cumulativePoints.plus(billingModel.billingAmount!!),
          modBy = storeBillingModel.regBy,
          modDate = DateUtil.nowLocalDateTime()
        ).let {
          nPointStoreRepository.update(it)
        }
      }
      return@withTransaction billingModel
    }
    storeBillingModel
  }

  override suspend fun cancelBilling(storeUid: String): Int = TransactionUtil.withTransaction {
    storeBillingRepository.cancelBilling(storeUid)
  }

}