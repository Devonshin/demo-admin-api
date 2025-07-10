package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.agency.bz.BzAgencyTable
import io.allink.receipt.api.domain.agency.bz.BzListAgencyModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable
import io.allink.receipt.api.repository.ExposedRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

interface StoreRepository : ExposedRepository<StoreTable, String, StoreModel> {
  override val table: StoreTable

  suspend fun findAll(filter: StoreFilter): PagedResult<StoreSearchModel>
  suspend fun findAll(filter: StoreFilter, bzAgencyUuid: UUID): PagedResult<StoreSearchModel>

  override fun toModel(row: ResultRow): StoreModel {

    val storeBilling = if (row[StoreBillingTable.id] != null) {
      StoreBillingModel(
        id = row[StoreBillingTable.id].value,
        storeUid = row[StoreBillingTable.storeUid],
        storeServiceSeq = row[StoreBillingTable.storeServiceSeq],
        tokenUuid = row[StoreBillingTable.tokenUuid],
        status = row[StoreBillingTable.status],
        billingAmount = row[StoreBillingTable.billingAmount],
        bankCode = row[StoreBillingTable.bankCode],
        bankAccountNo = row[StoreBillingTable.bankAccountNo],
        bankAccountName = row[StoreBillingTable.bankAccountName],
        regDate = row[StoreBillingTable.regDate],
        regBy = row[StoreBillingTable.regBy]
      )
    } else null
    val npointStore = if (row[NPointStoreTable.id] != null) {
      NPointStoreModel(
        id = row[NPointStoreTable.id],
        reservedPoints = row[NPointStoreTable.reservedPoints],
        reviewPoints = row[NPointStoreTable.reviewPoints],
        cumulativePoints = row[NPointStoreTable.cumulativePoints],
        regularPaymentAmounts = row[NPointStoreTable.regularPaymentAmounts],
        status = row[NPointStoreTable.status],
        serviceStartAt = row[NPointStoreTable.serviceStartAt],
        serviceEndAt = row[NPointStoreTable.serviceEndAt],
        pointRenewalType = row[NPointStoreTable.pointRenewalType],
        regDate = row[NPointStoreTable.regDate],
        modDate = row[NPointStoreTable.modDate],
        regBy = row[NPointStoreTable.regBy],
        modBy = row[NPointStoreTable.modBy]
      )
    } else null

    val bzAgency = if (row[BzAgencyTable.id] != null) {
      BzListAgencyModel(
        id = row[BzAgencyTable.id].value,
        agencyName = row[BzAgencyTable.agencyName],
        businessNo = row[BzAgencyTable.businessNo],
        status = row[BzAgencyTable.status],
      )
    } else null

    return StoreModel(
      id = row[table.id],
      storeName = row[table.storeName],
      zoneCode = row[table.zoneCode],
      addr1 = row[table.addr1],
      addr2 = row[table.addr2],
      deleteDate = row[table.deleteDate],
      franchiseCode = row[table.franchiseCode],
      mapUrl = row[table.mapUrl],
      lat = row[table.lat],
      lon = row[table.lon],
      tel = row[table.tel],
      mobile = row[table.mobile],
      managerName = row[table.managerName],
      siteLink = row[table.siteLink],
      workType = row[table.workType],
      businessNo = row[table.businessNo],
      ceoName = row[table.ceoName],
      businessType = row[table.businessType],
      eventType = row[table.eventType],
      email = row[table.email],
      businessNoLaw = row[table.businessNoLaw],
      storeType = row[table.storeType],
      iconUrl = row[table.iconUrl],
      logoUrl = row[table.logoUrl],
      receiptWidthInch = row[table.receiptWidthInch],
      status = row[table.status],
      partnerLoginId = row[table.partnerLoginId],
      partnerLoginPassword = null,
      couponAdYn = row[table.couponAdYn],
      applicationFilePath = row[table.applicationFilePath],
      bzFilePath = row[table.bzFilePath],
      idFilePath = row[table.idFilePath],
      bankFilePath = row[table.bankFilePath],
      storeBilling = storeBilling,
      nPointStore = npointStore,
      bzAgency = bzAgency,
      regBy = row[table.regBy],
      modBy = row[table.modBy],
      regDate = row[table.regDate],
      modDate = row[table.modDate],
    )
  }

  override fun toRow(model: StoreModel): StoreTable.(UpdateBuilder<*>) -> Unit = {
    it[id] = model.id!!
    it[storeName] = model.storeName
    it[zoneCode] = model.zoneCode
    it[addr1] = model.addr1
    it[addr2] = model.addr2
    it[franchiseCode] = model.franchiseCode
    it[mapUrl] = model.mapUrl
    it[lat] = model.lat
    it[lon] = model.lon
    it[tel] = model.tel
    it[mobile] = model.mobile
    it[managerName] = model.managerName
    it[siteLink] = model.siteLink
    it[workType] = model.workType
    it[businessNo] = model.businessNo
    it[ceoName] = model.ceoName
    it[businessType] = model.businessType
    it[eventType] = model.eventType
    it[email] = model.email
    it[businessNoLaw] = model.businessNoLaw
    it[storeType] = model.storeType
    it[iconUrl] = model.iconUrl
    it[logoUrl] = model.logoUrl
    it[status] = model.status
    it[receiptWidthInch] = model.receiptWidthInch
    it[partnerLoginId] = model.partnerLoginId
    it[partnerLoginPassword] = model.partnerLoginPassword
    it[applicationFilePath] = model.applicationFilePath
    it[bzFilePath] = model.bzFilePath
    it[idFilePath] = model.idFilePath
    it[bankFilePath] = model.bankFilePath
    it[regDate] = model.regDate
    it[regBy] = model.regBy
    it[deleteDate] = model.deleteDate
    it[couponAdYn] = model.couponAdYn
    it[bzAgencyId] = model.bzAgency?.id
  }

  override fun toUpdateRow(model: StoreModel): StoreTable.(UpdateStatement) -> Unit = {
    it[storeName] = model.storeName
    it[zoneCode] = model.zoneCode
    it[addr1] = model.addr1
    it[addr2] = model.addr2
    it[franchiseCode] = model.franchiseCode
    it[mapUrl] = model.mapUrl
    it[lat] = model.lat
    it[lon] = model.lon
    it[tel] = model.tel
    it[mobile] = model.mobile
    it[managerName] = model.managerName
    it[siteLink] = model.siteLink
    it[workType] = model.workType
    it[businessNo] = model.businessNo
    it[ceoName] = model.ceoName
    it[businessType] = model.businessType
    it[eventType] = model.eventType
    it[email] = model.email
    it[businessNoLaw] = model.businessNoLaw
    it[storeType] = model.storeType
    it[iconUrl] = model.iconUrl
    it[logoUrl] = model.logoUrl
    it[status] = model.status
    it[receiptWidthInch] = model.receiptWidthInch
    it[partnerLoginId] = model.partnerLoginId
    it[partnerLoginPassword] = model.partnerLoginPassword
    it[applicationFilePath] = model.applicationFilePath
    it[bzFilePath] = model.bzFilePath
    it[idFilePath] = model.idFilePath
    it[bankFilePath] = model.bankFilePath
    it[modDate] = model.modDate
    it[modBy] = model.modBy
    it[deleteDate] = model.deleteDate
    it[couponAdYn] = model.couponAdYn
    it[bzAgencyId] = model.bzAgency?.id
  }

  //  가맹점 등록
  override suspend fun create(model: StoreModel): StoreModel {
    table.insert(toRow(model))
    return model
  }

  //  가맹점 수정
  override suspend fun update(model: StoreModel): Int {
    return table.update({
      table.id eq model.id!!
    }) {
      toUpdateRow(model)(it)
    }
  }

  override suspend fun delete(id: String): Int {
    TODO()
  }

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      else when (column) {
        "id" -> StoreTable.id
        "businessNo" -> StoreTable.businessNo
        "name" -> StoreTable.storeName
        "franchiseCode" -> StoreTable.franchiseCode
        "regDate" -> StoreTable.regDate
        "modDate" -> StoreTable.modDate
        "addr1" -> StoreTable.addr1
        "managerName" -> StoreTable.managerName
        "ceoName" -> StoreTable.ceoName
        else -> null
      }
    }

  suspend fun searchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel>
  suspend fun find(id: String, agencyId: UUID): StoreModel?
  suspend fun findByNameAndBzNo(name: String, businessNo: String): StoreModel?
}