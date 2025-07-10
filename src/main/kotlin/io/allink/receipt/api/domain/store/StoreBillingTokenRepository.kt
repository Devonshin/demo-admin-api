package io.allink.receipt.api.domain.store

import io.allink.receipt.api.repository.ExposedRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.selectAll
import java.util.*

interface StoreBillingTokenRepository : ExposedRepository<StoreBillingTokenTable, UUID, StoreBillingTokenModel> {

  override fun toModel(row: ResultRow): StoreBillingTokenModel {
    return StoreBillingTokenModel(
      id = row[table.id],
      businessNo = row[table.businessNo],
      token = row[table.token],
      tokenInfo = row[table.tokenInfo],
      status = row[table.status],
      regDate = row[table.regDate],
      regBy = row[table.regBy]
    )
  }

  override fun toRow(model: StoreBillingTokenModel): StoreBillingTokenTable.(UpdateBuilder<*>) -> Unit = {
    it[table.id] = model.id!!
    it[table.businessNo] = model.businessNo
    it[table.token] = model.token
    it[table.tokenInfo] = model.tokenInfo
    it[table.status] = model.status
    it[table.regDate] = model.regDate
    it[table.regBy] = model.regBy
  }

  override fun toUpdateRow(model: StoreBillingTokenModel): StoreBillingTokenTable.(UpdateStatement) -> Unit {
    TODO("Not yet implemented")
  }

  override suspend fun create(model: StoreBillingTokenModel): StoreBillingTokenModel {
    TODO("Not yet implemented")
  }

  override suspend fun update(model: StoreBillingTokenModel): Int {
    TODO("Not yet implemented")
  }

  override suspend fun find(id: UUID): StoreBillingTokenModel? {
    return table.selectAll()
      .where {
        table.id eq id
      }
      .map { toModel(it) }
      .singleOrNull()
  }

  suspend fun findAllByBusinessNo(businessNo: String): List<StoreBillingTokenModel>?

  override suspend fun delete(id: UUID): Int {
    TODO("Not yet implemented")
  }

  override val columnConvert: (String?) -> Expression<out Any?>?
    get() = TODO("Not yet implemented")

  suspend fun cancelBilling(storeUid: String): Int
  suspend fun findAllActiveByBusinessNo(businessNo: String): List<StoreBillingTokenModel>?
}
