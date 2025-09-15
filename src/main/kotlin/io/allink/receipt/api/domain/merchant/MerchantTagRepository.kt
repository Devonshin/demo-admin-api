package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.merchant.MerchantTagTable.deviceId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.id
import io.allink.receipt.api.domain.merchant.MerchantTagTable.merchantGroupId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.merchantStoreId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.modDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.regDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.storeUid
import io.allink.receipt.api.domain.merchant.MerchantTagTable.tagName
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.repository.ExposedRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.select

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

interface MerchantTagRepository : ExposedRepository<MerchantTagTable, String, MerchantTagModel> {

  suspend fun findAll(filter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel>
  suspend fun findForUpdate(id: String): MerchantTagModel?

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      else when (column) {
        "id" -> table.id
        "name" -> table.tagName
        "franchiseCode" -> StoreTable.franchiseCode
        "regDate" -> StoreTable.regDate
        "modDate" -> StoreTable.modDate
        "storeName" -> StoreTable.storeName
        "businessNo" -> StoreTable.businessNo
        else -> null
      }
    }

  override fun toModel(row: ResultRow): MerchantTagModel {
    return Companion.toModel(row)
  }

  fun toUpdateModel(row: ResultRow): MerchantTagModel {
    return MerchantTagModel(
      id = row[id],
      tagName = row[tagName],
      merchantGroupId = row[merchantGroupId],
      merchantStoreId = row[merchantStoreId],
      deviceId = row[deviceId],
      storeUid = row[storeUid],
      regDate = row[regDate],
      modDate = row[modDate]
    )
  }

  override fun toRow(model: MerchantTagModel): MerchantTagTable.(UpdateBuilder<*>) -> Unit = {
    it[id] = model.id!!
    it[merchantGroupId] = model.merchantGroupId
    it[deviceId] = model.deviceId
    it[storeUid] = model.storeUid
    it[tagName] = model.tagName
    it[merchantStoreId] = model.merchantStoreId
    it[regDate] = model.regDate
    it[regBy] = model.regBy!!
  }

  override fun toUpdateRow(model: MerchantTagModel): MerchantTagTable.(UpdateStatement) -> Unit = {
    it[merchantGroupId] = model.merchantGroupId
    it[deviceId] = model.deviceId
    it[storeUid] = model.storeUid
    it[tagName] = model.tagName
    it[merchantStoreId] = model.merchantStoreId
    it[modDate] = model.modDate
    it[modBy] = model.modBy
  }

  override suspend fun find(id: String): MerchantTagModel? {
    return table
      .join(
        StoreTable,
        JoinType.LEFT,
        table.storeUid,
        StoreTable.id
      )
      .join(
        MerchantGroupTable,
        JoinType.LEFT,
        table.merchantGroupId,
        MerchantGroupTable.id
      )
      .select(
        table.id,
        table.merchantGroupId,
        table.deviceId,
        table.storeUid,
        table.merchantStoreId,
        table.tagName,
        table.regDate,
        table.modDate,
        StoreTable.id,
        StoreTable.storeName,
        StoreTable.businessNo,
        StoreTable.franchiseCode,
        StoreTable.ceoName,
        StoreTable.tel,
        StoreTable.businessType,
        StoreTable.eventType,
        StoreTable.status,
        StoreTable.regDate,
        StoreTable.deleteDate,
        StoreTable.modDate,
        MerchantGroupTable.id,
        MerchantGroupTable.receiptType,
      )
      .where { table.id eq id }
      .map { toModel(it) }
      .singleOrNull()
  }

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

  companion object {
    fun toModel(row: ResultRow): MerchantTagModel {
      val receiptType = row[MerchantGroupTable.receiptType]
      val deviceType = if (receiptType == "MERCHANT_RECEIPT") {
        "CAT"
      } else {
        "OKPOS"
      }
      return MerchantTagModel(
        id = row[id],
        store = SimpleMerchantStoreDetailModel(
          id = row[StoreTable.id],
          storeName = row[StoreTable.storeName],
          businessNo = row[StoreTable.businessNo],
          franchiseCode = row[StoreTable.franchiseCode],
          ceoName = row[StoreTable.ceoName],
          tel = row[StoreTable.tel],
          businessType = row[StoreTable.businessType],
          eventType = row[StoreTable.eventType],
          deviceType = deviceType,
          status = row[StoreTable.status],
          regDate = row[StoreTable.regDate],
          deleteDate = row[StoreTable.deleteDate],
          modDate = row[StoreTable.modDate],
        ),
        tagName = row[tagName],
        merchantGroupId = row[merchantGroupId],
        merchantStoreId = row[merchantStoreId],
        deviceId = row[deviceId],
        storeUid = row[storeUid],
        regDate = row[regDate],
        modDate = row[modDate]
      )
    }
  }
}