package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.merchant.MerchantTagTable.deviceId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.id
import io.allink.receipt.api.domain.merchant.MerchantTagTable.merchantGroupId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.merchantStoreId
import io.allink.receipt.api.domain.merchant.MerchantTagTable.modDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.regDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.storeUid
import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

interface MerchantTagRepository: ExposedRepository<MerchantTagTable, String, MerchantTagModel> {

  override fun toModel(row: ResultRow): MerchantTagModel {
    return Companion.toModel(row)
  }

  override fun toRow(model: MerchantTagModel): MerchantTagTable.(InsertStatement<EntityID<String>>) -> Unit = {
    it[merchantStoreId] = model.merchantStoreId
    it[merchantGroupId] = model.merchantGroupId
    it[deviceId] = model.deviceId
    it[storeUid] = model.storeUid
    it[regDate] = model.regDate
    it[modDate] = model.modDate
  }

  override fun toUpdateRow(model: MerchantTagModel): MerchantTagTable.(UpdateStatement) -> Unit = {
    it[merchantStoreId] = model.merchantStoreId
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

  override suspend fun find(id: String): MerchantTagModel? {
    TODO("Not yet implemented")
  }

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = TODO("Not yet implemented")

  companion object {
    fun toModel(row: ResultRow): MerchantTagModel {
      return MerchantTagModel(
        id = row[id],
        merchantStoreId = row[merchantStoreId],
        merchantGroupId = row[merchantGroupId],
        deviceId = row[deviceId],
        storeUid = row[storeUid],
        regDate = row[regDate],
        modDate = row[modDate]
      )
    }
  }
}