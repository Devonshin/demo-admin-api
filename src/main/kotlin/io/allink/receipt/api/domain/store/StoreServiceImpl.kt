import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.agency.bz.AgencyStatus
import io.allink.receipt.api.domain.agency.bz.BzListAgencyModel
import io.allink.receipt.api.domain.store.*
import io.allink.receipt.api.domain.store.npoint.*
import io.allink.receipt.api.exception.InvalidBillingStatusException
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.util.DateUtil
import io.ktor.server.plugins.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.YearMonth
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
      storeBillingTokens = findAllBillingToken(store.businessNo ?: "")
    )
  }

  override suspend fun findAllBillingToken(businessNo: String): List<StoreBillingTokenModel>? =
    TransactionUtil.withTransaction {
      storeBillingTokenRepository.findAllByBusinessNo(businessNo)
    }

  override suspend fun findStore(id: String, agencyId: UUID): StoreModel? = TransactionUtil.withTransaction {
    val store = storeRepository.find(id, agencyId)
    store?.copy(
      npointStoreServices = nPointStoreServiceService.getStoreServices(id),
      storeBillingTokens = findAllBillingToken(store.businessNo ?: "")
    )
  }

  override suspend fun findSearchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel> =
    TransactionUtil.withTransaction {
      storeRepository.searchStores(filter)
    }

  /*
  * 가맹점 등록
  * */
    override suspend fun registStore(
    storeRegistModel: StoreRegistModel,
    userUuid: UUID
  ): String = TransactionUtil.withTransaction {

    val existsStore = storeRepository.findByNameAndBzNo(storeRegistModel.storeName, storeRegistModel.businessNo)
    existsStore?.let {
      throw BadRequestException("동일한 사업자 정보로 등록된 가맹점이 있습니다. 가맹점명: [${existsStore.storeName}]")
    }
    val storeUid = UUID.randomUUID().toString()
    storeRepository.create(
      initStoreModel(storeUid, storeRegistModel, userUuid)
    )

    storeRegistModel.npointStoreServices?.let { selectedServices ->
      if (selectedServices.isEmpty()) {
        return@let
      }
      val now = DateUtil.nowLocalDateTime()
      val storeServiceSeq = DateUtil.nowInstant(now).epochSecond.toInt()
      val npointServices =
        nPointStoreServiceService.registNPointStoreService(selectedServices, storeUid, userUuid, storeServiceSeq, now)
      val storeBilling = storeRegistModel.storeBilling ?: throw BadRequestException("결제 정보 데이터가 필요합니다")

      val amountPair = storeBillingService.calculAmount(npointServices, now)
      val todayTotalPaymentAmount = amountPair.second

      if (storeBilling.billingAmount != todayTotalPaymentAmount) {
        throw BadRequestException("결제 금액이 올바르지 않습니다. billingAmount: ${storeBilling.billingAmount} shouldBe: $todayTotalPaymentAmount")
      }

      val initBillingModel =
        storeBillingService.initBillingModel(storeUid, storeServiceSeq, storeBilling, todayTotalPaymentAmount, now, userUuid)
      val registeredBilling = storeBillingService.registBilling(initBillingModel)
      logger.info("registeredBilling: $registeredBilling")

      val nPointStore = nPointStoreRepository.find(storeUid) ?: nPointStoreRepository.create(
        initNPointStoreModel(storeUid, now, userUuid)
      )
      logger.info("nPointStore: $nPointStore")
      logger.info("storeBilling: $storeBilling")
      logger.info("npointServices: $npointServices")
    }
    storeUid
  }

  /*
  * 가맹점 수정 시 포인트 서비스 신청 내용이 수정 됐을 경우, STANDBY: 즉시 결제(가맹점 등록 시점에 결제 실패의 경우), PENDING : 다음 달 1일 결제 예약
  * todo : 혹은 별도로 결제 api를 호출해야 한다.
  * todo : 익월 1일에 생성할 빌링 데이터는 가맹점 이용 서비스를 바탕으로 생성한다
  * todo : 가맹점 이용서비스를 중지 시키면 익월에 결제 할 내역도 더 이상 생성되지 않는다.
  * */
  override suspend fun modifyStore(
    storeModifyModel: StoreModifyModel,
    userUuid: UUID
  ): Unit = TransactionUtil.withTransaction {
    val storeUid = storeModifyModel.id
    storeRepository.update(modifyStoreModel(storeModifyModel, userUuid))

    storeModifyModel.npointStoreServices?.let { services ->
      if (services.isEmpty()) { //넘어온 서비스가 없을 경우 모두 취소 상태로 업데이트
        cancelNPointStore(storeUid, userUuid)
        return@withTransaction
      }

      val now = DateUtil.nowLocalDateTime()
      val storeServiceSeq = DateUtil.nowInstant(now).epochSecond.toInt()
      //새로운 포인트 서비스 생성 - 익월 1일에 결제 예정이 된다, 지금까지 추가된 서비스들은 대기 상태들은 취소처리되고 마지막에 등록된 서비스들로 익월에 시작
      nPointStoreServiceService.cancelNPointStoreServices(storeUid) //기존 등록 데이터 무효처리 - 활성화가 아닌 상태만 무효처리한다, 이후에 결제 시 기존 활성 서비스들은 만료처리
      val npointServices =
        nPointStoreServiceService.registNPointStoreService(services, storeUid, userUuid, storeServiceSeq, now)

      val storeBilling = storeModifyModel.storeBilling ?: throw BadRequestException("Store billing is required")
      val billingDay = if (storeBilling.status == BillingStatusCode.STANDBY) { //즉시 결제
        now
      } else {
        now.plusMonths(1).withDayOfMonth(1)
      }
      val amountPair = storeBillingService.calculAmount(npointServices, billingDay)
      val totalAmount = amountPair.second
      if (storeBilling.billingAmount != totalAmount) {
        throw BadRequestException("결제 금액이 올바르지 않습니다. billingAmount: ${storeBilling.billingAmount} shouldBe: $totalAmount")
      }

      logger.info("storeBilling: $storeBilling")
      logger.info("npointServices: $npointServices")
      //마지막에 등록된 빌링 데이터만 결제 요청에 사용된다.
      val initBillingModel =
        storeBillingService.initBillingModel(storeUid, storeServiceSeq, storeBilling, totalAmount, now, userUuid)
      //이전에 등록된, 결제 완료가 되지 않은 건들은 모두 취소처리
      storeBillingService.cancelBilling(storeUid)
      //결제 정보 추가
      val registeredBilling = storeBillingService.updateBilling(initBillingModel)
      val nPointStore = nPointStoreRepository.find(storeUid) ?: nPointStoreRepository.create(
        initNPointStoreModel(storeUid, now, userUuid)
      )
      logger.info("nPointStore: $nPointStore")
      logger.info("registeredBilling: $registeredBilling")
    }
  }

  private suspend fun cancelNPointStore(storeUid: String, userUuid: UUID) {
    nPointStoreServiceService.cancelNPointStoreServices(storeUid)
    nPointStoreRepository.find(storeUid)?.let {
      nPointStoreRepository.update(
        it.copy(
          status = StatusCode.DELETED,
          modDate = DateUtil.nowLocalDateTime(),
          modBy = userUuid,
        )
      )
    }
  }

  private fun initNPointStoreModel(
    storeUid: String,
    now: LocalDateTime,
    userUuid: UUID
  ): NPointStoreModel = NPointStoreModel(
    id = storeUid,
    reservedPoints = 0,
    reviewPoints = 0,
    cumulativePoints = 0,
    regularPaymentAmounts = 0,
    status = StatusCode.PENDING, //초기엔 대기 상태
    serviceStartAt = now,
    pointRenewalType = PointRenewalType.AUTO_RENEWAL,
    regDate = now,
    regBy = userUuid
  )

  private fun initStoreModel(
    storeId: String,
    storeRegistModel: StoreRegistModel,
    userUuid: UUID
  ): StoreModel = StoreModel(
    id = storeId,
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