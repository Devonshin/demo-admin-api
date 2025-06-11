package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import io.allink.receipt.api.domain.PagedResult
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

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

interface StoreRepository : ExposedRepository<StoreTable, String, StoreModel> {
  override val table: StoreTable

  suspend fun findAll(filter: StoreFilter): PagedResult<StoreModel>

  override fun toModel(row: ResultRow): StoreModel {
    return Companion.toModel(row)
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
    it[regDate] = model.regDate
    it[regBy] = model.regBy
    it[deleteDate] = model.deleteDate
    it[couponAdYn] = model.couponAdYn
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
    it[modDate] = model.modDate
    it[modBy] = model.modBy
    it[deleteDate] = model.deleteDate
    it[couponAdYn] = model.couponAdYn
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

  override suspend fun find(id: String): StoreModel? {
    return table
      .join(StoreBillingTable, JoinType.LEFT, table.id, StoreBillingTable.storeUid)
      .join(NPointStoreTable, JoinType.LEFT, table.id, NPointStoreTable.id)
      .selectAll()
      .where { table.id eq id }
      .orderBy(
        StoreBillingTable.id, SortOrder.DESC
      )
      .limit(1)
      .map { toModel(it) }
      .singleOrNull()
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

  companion object {
    fun toModel(row: ResultRow): StoreModel {
      return StoreModel(
        id = row[StoreTable.id],
        storeName = row[StoreTable.storeName],
        zoneCode = row[StoreTable.zoneCode],
        addr1 = row[StoreTable.addr1],
        addr2 = row[StoreTable.addr2],
        deleteDate = row[StoreTable.deleteDate],
        franchiseCode = row[StoreTable.franchiseCode],
        mapUrl = row[StoreTable.mapUrl],
        lat = row[StoreTable.lat],
        lon = row[StoreTable.lon],
        tel = row[StoreTable.tel],
        mobile = row[StoreTable.mobile],
        managerName = row[StoreTable.managerName],
        siteLink = row[StoreTable.siteLink],
        workType = row[StoreTable.workType],
        businessNo = row[StoreTable.businessNo],
        ceoName = row[StoreTable.ceoName],
        businessType = row[StoreTable.businessType],
        eventType = row[StoreTable.eventType],
        email = row[StoreTable.email],
        businessNoLaw = row[StoreTable.businessNoLaw],
        storeType = row[StoreTable.storeType],
        iconUrl = row[StoreTable.iconUrl],
        logoUrl = row[StoreTable.logoUrl],
        receiptWidthInch = row[StoreTable.receiptWidthInch],
        status = row[StoreTable.status],
        partnerLoginId = row[StoreTable.partnerLoginId],
        partnerLoginPassword = row[StoreTable.partnerLoginPassword],
        regBy = row[StoreTable.regBy],
        modBy = row[StoreTable.modBy],
        regDate = row[StoreTable.regDate],
        modDate = row[StoreTable.modDate],
        couponAdYn = row[StoreTable.couponAdYn],
        storeBilling = StoreBillingModel(
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
        ),
        nPointStore = NPointStoreModel(
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
      )
    }
  }

  suspend fun searchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel>
}