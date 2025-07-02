import io.allink.receipt.api.common.Constant
import io.allink.receipt.api.common.Constant.Companion.ERECEIPT
import io.allink.receipt.api.common.Constant.Companion.REVIEWPT
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.agency.bz.AgencyStatus
import io.allink.receipt.api.domain.agency.bz.BzListAgencyModel
import io.allink.receipt.api.domain.store.*
import io.allink.receipt.api.domain.store.npoint.NPointStoreModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreRepository
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceService
import io.allink.receipt.api.domain.store.npoint.PointRenewalType
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.util.DateUtil
import io.ktor.server.plugins.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

class StoreServiceImpl(
  private val storeRepository: StoreRepository,
  private val nPointStoreRepository: NPointStoreRepository,
  private val nPointStoreServiceService: NPointStoreServiceService,
  private val storeBillingService: StoreBillingService,
  private val storeBillingTokenRepository: StoreBillingTokenRepository,
) : StoreService {

  val logger: Logger = LoggerFactory.getLogger(StoreServiceImpl::class.java)

  override suspend fun findAllAgencyStore(
    filter: StoreFilter,
    agencyId: UUID
  ): PagedResult<StoreSearchModel> = TransactionUtil.withTransaction {
    storeRepository.findAll(filter, agencyId)
  }

  override suspend fun findAllStore(
    filter: StoreFilter
  ): PagedResult<StoreSearchModel> = TransactionUtil.withTransaction {
    storeRepository.findAll(filter)
  }

  override suspend fun findStore(id: String): StoreModel? = TransactionUtil.withTransaction {
    val store = storeRepository.find(id)
    store?.copy(
      npointStoreServices = nPointStoreServiceService.getStoreServices(id),
      storeBillingTokens = storeBillingTokenRepository.findAllByBusinessNo(store.businessNo ?: "")
    )
  }

  override suspend fun findStore(id: String, agencyId: UUID): StoreModel? = TransactionUtil.withTransaction {
    val store = storeRepository.find(id, agencyId)
    store?.copy(
      npointStoreServices = nPointStoreServiceService.getStoreServices(id),
      storeBillingTokens = storeBillingTokenRepository.findAllByBusinessNo(store.businessNo ?: "")
    )
  }

  override suspend fun findSearchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel> =
    TransactionUtil.withTransaction {
      storeRepository.searchStores(filter)
    }

  override suspend fun registStore(
    storeRegistModel: StoreRegistModel,
    userUuid: UUID
  ): String = TransactionUtil.withTransaction {
    val storeUid = UUID.randomUUID().toString()
    storeRepository.create(
      initStoreModel(storeUid, storeRegistModel, userUuid)
    )

    storeRegistModel.npointStoreServices?.let { services ->
      if (services.isEmpty()) {
        return@let
      }

      val storeBilling = storeRegistModel.storeBilling ?: throw BadRequestException("결제 정보 데이터가 필요합니다")
      if (storeBilling.tokenUuid.isEmpty()) {
        throw BadRequestException("결제 토큰이 유효하지 않습니다. tokenUuid")
      }

      val now = DateUtil.nowLocalDateTime()
      val yyMMddHHmm = DateUtil.nowInstant(now).epochSecond.toString().takeLast(10)

      val registeredServices =
        nPointStoreServiceService.registNPointStoreReviewService(services, storeUid, userUuid, yyMMddHHmm, now)
      var totalAmount = 0
      var rewardPoint = 0
      for (registeredService in registeredServices) {
        totalAmount += registeredService.serviceCharge!! + registeredService.rewardDeposit!!
        if (registeredService.serviceCode == REVIEWPT) {
          val serviceCodes = nPointStoreServiceService.getServiceCodes()
          val ereceipt = serviceCodes[ERECEIPT]
          totalAmount -= (registeredService.serviceCharge / 30) * ((30 - now.dayOfMonth + 1)) + (ereceipt?.price!! / 30) * ((30 - now.dayOfMonth + 1))
        }
        rewardPoint += registeredService.rewardPoint!!
      }

      if (storeBilling.billingAmount != totalAmount) {
        throw BadRequestException("결제 금액이 올바르지 않습니다. billingAmount: ${storeBilling.billingAmount} shouldBe: $totalAmount")
      }

      val nPointStore = nPointStoreRepository.find(storeUid) ?: nPointStoreRepository.create(
        NPointStoreModel(
          id = storeUid,
          reservedPoints = 0,
          reviewPoints = 0,
          cumulativePoints = 0,
          regularPaymentAmounts = 0,
          status = StatusCode.ACTIVE,
          serviceStartAt = now,
          pointRenewalType = PointRenewalType.AUTO_RENEWAL,
          regDate = now,
          regBy = userUuid
        )
      )

      nPointStore.reviewPoints = rewardPoint
      nPointStore.modDate = now
      nPointStore.modBy = userUuid
      nPointStoreRepository.update(nPointStore)

      logger.info("nPointStore: $nPointStore")
      logger.info("storeBilling: $storeBilling")
      logger.info("registeredServices: $registeredServices")
      val registeredBilling = storeBillingService.registBilling(
        StoreBillingModel(
          storeUid = storeUid,
          storeServiceSeq = yyMMddHHmm,
          tokenUuid = UUID.fromString(storeBilling.tokenUuid),
          billingAmount = totalAmount,
          bankCode = storeBilling.bankCode,
          bankAccountNo = storeBilling.bankAccountNo,
          bankAccountName = storeBilling.bankAccountName,
          regDate = now,
          regBy = userUuid
        )
      )
      logger.info("registeredBilling: $registeredBilling")
    }
    storeUid
  }

  override suspend fun modifyStore(
    storeModifyModel: StoreModifyModel,
    userUuid: UUID
  ): Unit = TransactionUtil.withTransaction {
    val storeUid = storeModifyModel.id
    storeRepository.update(modifyStoreModel(storeModifyModel, userUuid))

    storeModifyModel.npointStoreServices?.let { services ->
      if (services.isEmpty()) {
        nPointStoreServiceService.cancelNPointStoreServices(storeUid)
        return@let
      }

      val now = DateUtil.nowLocalDateTime()
      val yyMMddHHmm = DateUtil.nowInstant(now).epochSecond.toString().takeLast(10)
      val registeredServices =
        nPointStoreServiceService.registNPointStoreReviewService(services, storeUid, userUuid, yyMMddHHmm, now)

      val storeBilling = storeModifyModel.storeBilling ?: throw BadRequestException("Store billing is required")
      var amount = 0
      var rewardPoint = 0
      for (registeredService in registeredServices) {
        amount += registeredService.serviceCharge!! + registeredService.rewardDeposit!!
        rewardPoint += registeredService.rewardPoint!!
      }

      val nPointStore = nPointStoreRepository.find(storeUid) ?: nPointStoreRepository.create(
        NPointStoreModel(
          id = storeUid,
          reservedPoints = 0,
          reviewPoints = 0,
          cumulativePoints = 0,
          regularPaymentAmounts = 0,
          status = StatusCode.ACTIVE,
          serviceStartAt = now,
          pointRenewalType = PointRenewalType.AUTO_RENEWAL,
          regDate = now,
          regBy = userUuid
        )
      )

      nPointStore.reviewPoints = rewardPoint
      nPointStore.modDate = now
      nPointStore.modBy = userUuid
      nPointStoreRepository.update(nPointStore)
      storeBillingService.cancelBilling(storeUid)
      logger.info("nPointStore: $nPointStore")
      logger.info("storeBilling: $storeBilling")
      logger.info("registeredServices: $registeredServices")
      val registeredBilling = storeBillingService.registBilling(
        StoreBillingModel(
          storeUid = storeUid,
          storeServiceSeq = yyMMddHHmm,
          tokenUuid = UUID.fromString(storeBilling.tokenUuid),
          billingAmount = amount,
          bankCode = storeBilling.bankCode,
          bankAccountNo = storeBilling.bankAccountNo,
          bankAccountName = storeBilling.bankAccountName,
          regDate = now,
          regBy = userUuid
        )
      )
      logger.info("registeredBilling: $registeredBilling")
    }
  }


  private fun initStoreModel(
    storeUUID: String,
    storeRegistModel: StoreRegistModel,
    userUuid: UUID
  ): StoreModel = StoreModel(
    id = storeUUID,
    storeName = storeRegistModel.storeName,
    franchiseCode = storeRegistModel.franchiseCode,
    addr1 = storeRegistModel.addr1,
    addr2 = storeRegistModel.addr2,
    tel = storeRegistModel.tel,
    mobile = storeRegistModel.mobile,
    managerName = storeRegistModel.managerName,
    workType = storeRegistModel.workType,
    businessNo = storeRegistModel.businessNo,
    businessNoLaw = storeRegistModel.businessNoLaw,
    ceoName = storeRegistModel.ceoName,
    businessType = storeRegistModel.businessType,
    eventType = storeRegistModel.eventType,
    email = storeRegistModel.email,
    status = storeRegistModel.status,
    couponAdYn = storeRegistModel.couponAdYn,
    bzAgency = BzListAgencyModel(
      id = storeRegistModel.bzAgencyId?.let { UUID.fromString(it) },
      agencyName = null,
      businessNo = null,
      status = AgencyStatus.ACTIVE,
    ),
    regDate = DateUtil.nowLocalDateTime(),
    regBy = userUuid
  )

  private fun modifyStoreModel(
    storeModifyModel: StoreModifyModel,
    userUuid: UUID
  ): StoreModel = StoreModel(
    id = storeModifyModel.id,
    storeName = storeModifyModel.storeName,
    franchiseCode = storeModifyModel.franchiseCode,
    addr1 = storeModifyModel.addr1,
    addr2 = storeModifyModel.addr2,
    tel = storeModifyModel.tel,
    mobile = storeModifyModel.mobile,
    managerName = storeModifyModel.managerName,
    workType = storeModifyModel.workType,
    businessNo = storeModifyModel.businessNo,
    businessNoLaw = storeModifyModel.businessNoLaw,
    ceoName = storeModifyModel.ceoName,
    businessType = storeModifyModel.businessType,
    eventType = storeModifyModel.eventType,
    email = storeModifyModel.email,
    status = storeModifyModel.status,
    couponAdYn = storeModifyModel.couponAdYn,
    bzAgency = BzListAgencyModel(
      id = storeModifyModel.bzAgencyId?.let { UUID.fromString(it) },
      agencyName = null,
      businessNo = null,
      status = AgencyStatus.ACTIVE,
    ),
    modDate = DateUtil.nowLocalDateTime(),
    modBy = userUuid
  )
}