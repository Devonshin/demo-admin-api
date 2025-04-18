package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.common.ExposedRepository
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

  override fun toRow(model: MerchantTagModel): MerchantTagTable.(InsertStatement<EntityID<String>>) -> Unit {
    TODO("Not yet implemented")
  }

  override fun toUpdateRow(model: MerchantTagModel): MerchantTagTable.(UpdateStatement) -> Unit {
    TODO("Not yet implemented")
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
        id = row[MerchantTagTable.id],
        merchantStoreId = row[MerchantTagTable.merchantStoreId],
        merchantGroupId = row[MerchantTagTable.merchantGroupId],
        deviceId = row[MerchantTagTable.deviceId],
        storeUid = row[MerchantTagTable.storeUid],
        regDate = row[MerchantTagTable.regDate],
        modDate = row[MerchantTagTable.modDate]
      )
    }
  }
}