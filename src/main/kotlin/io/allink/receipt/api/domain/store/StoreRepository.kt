package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

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

  override fun toRow(model: StoreModel): StoreTable.(InsertStatement<EntityID<String>>) -> Unit = {
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
    it[receiptWidthInch] = model.receiptWidthInch
    it[partnerLoginId] = model.partnerLoginId
    it[partnerLoginPassword] = model.partnerLoginPassword
    it[regDate] = model.regDate
    it[deleteDate] = model.deleteDate
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
    it[receiptWidthInch] = model.receiptWidthInch
    it[partnerLoginId] = model.partnerLoginId
    it[partnerLoginPassword] = model.partnerLoginPassword
    it[modDate] = model.modDate
    it[deleteDate] = model.deleteDate
  }

  override suspend fun create(model: StoreModel): StoreModel = query {
    TODO()
  }

  override suspend fun update(model: StoreModel): Int = query {
    TODO()
  }

  override suspend fun find(id: String): StoreModel? = query {
    table.selectAll()
      .where { table.id eq id }
      .map { toModel(it) }
      .singleOrNull()
  }

  override suspend fun delete(id: String): Int = query {
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
        regDate = row[StoreTable.regDate],
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
        modDate = row[StoreTable.modDate],
        storeType = row[StoreTable.storeType],
        iconUrl = row[StoreTable.iconUrl],
        logoUrl = row[StoreTable.logoUrl],
        receiptWidthInch = row[StoreTable.receiptWidthInch],
        status = row[StoreTable.status],
        partnerLoginId = row[StoreTable.partnerLoginId],
        partnerLoginPassword = row[StoreTable.partnerLoginPassword]
      )
    }
  }

  suspend fun searchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel>
}