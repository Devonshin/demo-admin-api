package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.cumulativePoints
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.modDate
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.pointRenewalType
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.regularPaymentAmounts
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.reservedPoints
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.reviewPoints
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.serviceEndAt
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.serviceStartAt
import io.allink.receipt.api.domain.store.npoint.NPointStoreTable.status
import io.allink.receipt.api.repository.ExposedRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

interface NPointStoreRepository : ExposedRepository<NPointStoreTable, String, NPointStoreModel> {

  override fun toModel(row: ResultRow): NPointStoreModel {
    return NPointStoreModel(
      id = row[table.id],
      reservedPoints = row[reservedPoints],
      reviewPoints = row[reviewPoints],
      cumulativePoints = row[cumulativePoints],
      regularPaymentAmounts = row[regularPaymentAmounts],
      status = row[status],
      serviceStartAt = row[serviceStartAt],
      serviceEndAt = row[serviceEndAt],
      pointRenewalType = row[pointRenewalType],
      regDate = row[table.regDate],
      modDate = row[modDate],
      regBy = row[table.regBy],
      modBy = row[table.modBy],
    )
  }

  override fun toRow(model: NPointStoreModel): NPointStoreTable.(UpdateBuilder<*>) -> Unit = {
    it[table.id] = model.id!!
    it[reservedPoints] = model.reservedPoints
    it[reviewPoints] = model.reviewPoints
    it[cumulativePoints] = model.cumulativePoints
    it[regularPaymentAmounts] = model.regularPaymentAmounts
    it[status] = model.status
    it[serviceStartAt] = model.serviceStartAt
    it[serviceEndAt] = model.serviceEndAt
    it[pointRenewalType] = model.pointRenewalType
    it[regDate] = model.regDate
    it[regBy] = model.regBy
  }

  override fun toUpdateRow(model: NPointStoreModel): NPointStoreTable.(UpdateStatement) -> Unit = {
    it[reservedPoints] = model.reservedPoints
    it[reviewPoints] = model.reviewPoints
    it[cumulativePoints] = model.cumulativePoints
    it[regularPaymentAmounts] = model.regularPaymentAmounts
    it[status] = model.status
    it[serviceStartAt] = model.serviceStartAt
    it[serviceEndAt] = model.serviceEndAt
    it[pointRenewalType] = model.pointRenewalType
    it[modDate] = model.modDate
    it[modBy] = model.modBy
  }

  override suspend fun create(model: NPointStoreModel): NPointStoreModel {
    val inserted = table.insert {
      toRow(model)(it)
    }
    return model.copy(id = inserted[table.id])
  }

  override suspend fun update(model: NPointStoreModel): Int {
    return table.update({ table.id eq model.id!! }) {
      toUpdateRow(model)(it)
    }
  }

  override suspend fun find(id: String): NPointStoreModel? {
    return table.selectAll()
      .where {
        table.id eq id
      }
      .map {
        toModel(it)
      }
      .singleOrNull()
  }

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

  override val columnConvert: (String?) -> Expression<out Any?>?
    get() = TODO("Not yet implemented")

}
