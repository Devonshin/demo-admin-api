package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.merchant.MerchantTagTable.deviceId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.id
import io.allink.receipt.api.domain.merchant.MerchantTagTable.merchantGroupId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.modDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.regDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.storeUid
import io.allink.receipt.api.domain.merchant.MerchantTagTable.tagName
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

interface MerchantTagRepository : ExposedRepository<MerchantTagTable, String, MerchantTagModel> {

  suspend fun findAll(filter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel> = query {

    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table
      .join(
        StoreTable,
        JoinType.LEFT,
        table.storeUid,
        StoreTable.id
      )
      .select(
        table.id,
        table.merchantGroupId,
        table.storeUid,
        table.regDate,
        table.modDate,
        StoreTable.id,
        StoreTable.storeName,
        StoreTable.franchiseCode,
        StoreTable.businessNo,
        StoreTable.regDate,
        StoreTable.modDate,
        StoreTable.deleteDate,
        StoreTable.status,
      )

    filter.id?.let { select.andWhere { table.id eq it } }

    filter.storeId?.let { select.andWhere { table.storeUid eq it } }

    filter.businessNo?.let { select.andWhere { StoreTable.businessNo eq it } }

    filter.storeName?.let { select.andWhere { StoreTable.storeName like "it%" } }

    filter.franchiseCode?.let { select.andWhere { StoreTable.franchiseCode eq it } }

    filter.period.let {
      it.from.let { from ->
        select.andWhere { table.regDate greaterEq from }
      }
      it.to.let { to ->
        select.andWhere { table.regDate lessEq to }
      }
    }

    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map {
        SimpleMerchantTagModel(
          id = it[id],
          store = SimpleMerchantTagStoreModel(
            id = it[storeUid],
            storeName = it[StoreTable.storeName],
            businessNo = it[StoreTable.businessNo],
            franchiseCode = it[StoreTable.franchiseCode],
            status = it[StoreTable.status]
          ),
          regDate = it[regDate],
          modDate = it[modDate]
        )
      }
    return@query PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      else when (column) {
        "id" -> StoreTable.id
        "name" -> StoreTable.storeName
        "franchiseCode" -> StoreTable.franchiseCode
        "regDate" -> StoreTable.regDate
        "modDate" -> StoreTable.modDate
        "storeName" -> StoreTable.addr1
        "storeStatus" -> StoreTable.managerName
        "businessNo" -> StoreTable.businessNo
        else -> null
      }
    }

  override fun toModel(row: ResultRow): MerchantTagModel {
    return Companion.toModel(row)
  }

  override fun toRow(model: MerchantTagModel): MerchantTagTable.(InsertStatement<EntityID<String>>) -> Unit = {
    it[merchantGroupId] = model.merchantGroupId
    it[deviceId] = model.deviceId
    it[storeUid] = model.storeUid
    it[regDate] = model.regDate
    it[modDate] = model.modDate
  }

  override fun toUpdateRow(model: MerchantTagModel): MerchantTagTable.(UpdateStatement) -> Unit = {
    it[merchantGroupId] = model.merchantGroupId
    it[deviceId] = model.deviceId
    it[storeUid] = model.storeUid
    it[regDate] = model.regDate
    it[modDate] = model.modDate
  }

  override suspend fun create(model: MerchantTagModel): MerchantTagModel {
    TODO("Not yet implemented")
  }

  override suspend fun update(model: MerchantTagModel): Int {
    TODO("Not yet implemented")
  }

  override suspend fun find(id: String): MerchantTagModel? = query {
    table
      .join(StoreTable, JoinType.LEFT, table.storeUid, StoreTable.id)
      .selectAll().where { table.id eq id }.map { toModel(it) }.singleOrNull()
  }

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

  companion object {
    fun toModel(row: ResultRow): MerchantTagModel {
      return MerchantTagModel(
        id = row[id],
        store = SimpleMerchantStoreDetailModel(
          id = row[StoreTable.id],
          storeName = row[StoreTable.storeName],
          businessNo = row[StoreTable.businessNo],
          franchiseCode = row[StoreTable.franchiseCode],
          ceoName = row[StoreTable.ceoName],
          businessType = row[StoreTable.businessType],
          eventType = row[StoreTable.eventType],
          status = row[StoreTable.status],
          regDate = row[StoreTable.regDate],
          deleteDate = row[StoreTable.deleteDate],
          modDate = row[StoreTable.modDate],
        ),
        tagName = row[tagName],
        merchantGroupId = row[merchantGroupId],
        deviceId = row[deviceId],
        storeUid = row[storeUid],
        regDate = row[regDate],
        modDate = row[modDate]
      )
    }
  }
}