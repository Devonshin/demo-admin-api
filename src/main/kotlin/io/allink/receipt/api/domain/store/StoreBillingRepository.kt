package io.allink.receipt.api.domain.store

import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.update

interface StoreBillingRepository : ExposedRepository<StoreBillingTable, Long, StoreBillingModel> {

  override fun toModel(row: ResultRow): StoreBillingModel {
    return StoreBillingModel(
      id = row[table.id].value,
      storeUid = row[table.storeUid],
      storeServiceSeq = row[table.storeServiceSeq],
      tokenUuid = row[table.tokenUuid],
      status = row[table.status],
      billingAmount = row[table.billingAmount],
      bankCode = row[table.bankCode],
      bankAccountNo = row[table.bankAccountNo],
      bankAccountName = row[table.bankAccountName],
      regDate = row[table.regDate],
      regBy = row[table.regBy],
    )
  }

  override fun toRow(model: StoreBillingModel): StoreBillingTable.(UpdateBuilder<*>) -> Unit = {
    it[table.storeUid] = model.storeUid
    it[table.tokenUuid] = model.tokenUuid
    it[table.storeServiceSeq] = model.storeServiceSeq
    it[table.status] = model.status!!
    it[table.billingAmount] = model.billingAmount
    it[table.bankCode] = model.bankCode
    it[table.bankAccountNo] = model.bankAccountNo
    it[table.bankAccountName] = model.bankAccountName
    it[table.regDate] = model.regDate
    it[table.regBy] = model.regBy
  }

  override fun toUpdateRow(model: StoreBillingModel): StoreBillingTable.(UpdateStatement) -> Unit = {
    it[table.status] = model.status!!
  }

  override suspend fun create(model: StoreBillingModel): StoreBillingModel {
    val insert = table.insert {
      toRow(model)(it)
    }
    return model.copy(id = insert[table.id].value)
  }

  override suspend fun update(model: StoreBillingModel): Int {
    return table.update(
      where = { table.id eq model.id!! },
      body = { it[table.status] = model.status!! }
    )
  }

  override suspend fun find(id: Long): StoreBillingModel? {
    TODO("Not yet implemented")
  }

  override suspend fun delete(id: Long): Int {
    TODO("Not yet implemented")
  }

  override val columnConvert: (String?) -> Expression<out Any?>?
    get() = TODO("Not yet implemented")

  suspend fun cancelBilling(storeUid: String): Int
}
