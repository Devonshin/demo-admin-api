package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.ExposedRepository
import io.allink.receipt.api.common.PagedResult
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

  override fun toModel(row: ResultRow): StoreModel {
    return StoreModel(
      id = row[table.id],
      storeName = row[table.storeName],
      zoneCode = row[table.zoneCode],
      addr1 = row[table.addr1],
      addr2 = row[table.addr2],
      regDate = row[table.regDate],
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
      modDate = row[table.modDate],
      storeType = row[table.storeType],
      iconUrl = row[table.iconUrl],
      logoUrl = row[table.logoUrl],
      receiptWidthInch = row[table.receiptWidthInch],
      partnerLoginId = row[table.partnerLoginId],
      partnerLoginPassword = row[table.partnerLoginPword]
    )
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
    it[partnerLoginPword] = model.partnerLoginPassword
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
    it[partnerLoginPword] = model.partnerLoginPassword
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
    table.selectAll().where { table.id eq id }.map { toModel(it) }.singleOrNull()
  }

  override suspend fun delete(id: String): Int = query {
    TODO()
  }

  suspend fun findAll(filter: StoreFilter): PagedResult<StoreModel>

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      else when (column) {
        "id" -> StoreTable.id
        "businessNo" -> StoreTable.businessNo
        "storeName" -> StoreTable.storeName
        "franchiseCode" -> StoreTable.franchiseCode
        else -> null
        }
    }
}