package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.domain.code.ServiceCodeModel
import io.allink.receipt.api.domain.code.ServiceCodeTable
import io.allink.receipt.api.repository.ExposedRepository
import io.ktor.server.plugins.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

interface NPointStoreServiceRepository :
  ExposedRepository<NPointStoreServiceTable, String, NPointStoreServiceModel> {

  override fun toModel(row: ResultRow): NPointStoreServiceModel {
    return NPointStoreServiceModel(
      id = NPointStoreServiceId(
        storeServiceSeq = row[table.id],
        storeUid = row[table.storeUid],
        serviceCode = row[table.serviceCode]
      ),
      service = ServiceCodeModel(
        id = row[ServiceCodeTable.serviceCode],
        serviceGroup = row[ServiceCodeTable.serviceGroup],
        serviceName = row[ServiceCodeTable.serviceName],
        price = row[ServiceCodeTable.price],
        status = row[ServiceCodeTable.status],
        serviceType = row[ServiceCodeTable.serviceType],
      ),
      serviceCharge = row[table.serviceCharge],
      rewardDeposit = row[table.rewardDeposit],
      rewardPoint = row[table.rewardPoint],
      serviceCommission = row[table.serviceCommission],
      status = row[table.status],
      regDate = row[table.regDate],
      modDate = row[table.modDate],
      regBy = row[table.regBy],
      modBy = row[table.modBy],
    )
  }

  override fun toRow(model: NPointStoreServiceModel): NPointStoreServiceTable.(UpdateBuilder<*>) -> Unit =
    {
      it[table.id] = model.id?.storeServiceSeq!!
      it[table.storeUid] = model.id?.storeUid!!
      it[table.serviceCode] = model.id?.serviceCode!!
      it[table.serviceCharge] = model.serviceCharge
      it[table.rewardDeposit] = model.rewardDeposit
      it[table.rewardPoint] = model.rewardPoint
      it[table.serviceCommission] = model.serviceCommission
      it[table.status] = model.status
      it[table.regDate] = model.regDate
      it[table.regBy] = model.regBy
    }

  override fun toUpdateRow(model: NPointStoreServiceModel): NPointStoreServiceTable.(UpdateStatement) -> Unit = {
    it[table.serviceCharge] = model.serviceCharge
    it[table.rewardDeposit] = model.rewardDeposit
    it[table.rewardPoint] = model.rewardPoint
    it[table.serviceCommission] = model.serviceCommission
    it[table.status] = model.status
    it[table.modDate] = model.modDate
    it[table.modBy] = model.modBy
  }

  override suspend fun create(model: NPointStoreServiceModel): NPointStoreServiceModel {
    table.insert(toRow(model))
    return model
  }

  override suspend fun update(model: NPointStoreServiceModel): Int {
    val modelId = model.id ?: throw BadRequestException("Model ID cannot be null")
    return table.update({
      table.id eq modelId.storeServiceSeq
      table.storeUid eq modelId.storeUid
      table.serviceCode eq modelId.serviceCode
    }) {
      toUpdateRow(model)
    }
  }

  suspend fun find(id: NPointStoreServiceId): NPointStoreServiceModel? {
    return table
      .join(ServiceCodeTable, JoinType.LEFT, table.serviceCode, ServiceCodeTable.serviceCode)
      .selectAll()
      .where {
        table.id eq id.storeServiceSeq
        table.storeUid eq id.storeUid
        table.serviceCode eq id.serviceCode
      }
      .map {
        toModel(it)
      }
      .singleOrNull()
  }

  suspend fun findAllStoreService(storeUid: String): Map<String, List<NPointStoreServiceModel>>
  suspend fun findAllStoreService(yyMMddHHmm: String, storeUid: String): List<NPointStoreServiceModel>

  suspend fun delete(id: NPointStoreServiceId): Int {
    TODO("Not yet implemented")
  }

  override val columnConvert: (String?) -> Expression<out Any?>?
    get() = TODO("Not yet implemented")

  suspend fun cancelAllStoreService(storeUid: String): Int
}
