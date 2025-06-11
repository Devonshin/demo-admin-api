package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.common.Constant
import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.code.ServiceCodeTable
import io.allink.receipt.api.util.DateUtil
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

class NPointStoreServiceRepositoryImpl(
  override val table: NPointStoreServiceTable
) : NPointStoreServiceRepository {
  override suspend fun findAllStoreService(storeUid: String): Map<String, List<NPointStoreServiceModel>> {
    return table
      .join(ServiceCodeTable, JoinType.LEFT, table.serviceCode, ServiceCodeTable.serviceCode)
      .selectAll()
      .where {
        table.storeUid eq storeUid
      }
      .map {
        toModel(it)
      }
      .toList()
      .groupBy {
        it.id?.storeServiceSeq!!
      }
  }

  override suspend fun findAllStoreService(yyMMddHHmm: String, storeUid: String): List<NPointStoreServiceModel> {
    return table
      .join(ServiceCodeTable, JoinType.LEFT, table.serviceCode, ServiceCodeTable.serviceCode)
      .selectAll()
      .where {
        table.id eq yyMMddHHmm
        table.storeUid eq storeUid
      }
      .map {
        toModel(it)
      }
      .toList()
  }

  override suspend fun cancelAllStoreService(storeUid: String): Int {
    return table.update(
      where = {
        table.storeUid eq storeUid
      }
    ) {
      it[status] = StatusCode.INACTIVE
      it[modBy] = Constant.Companion.SYSTEM_UUID
      it[modDate] = DateUtil.nowLocalDateTime()
    }
  }

  override suspend fun find(id: String): NPointStoreServiceModel? {
    TODO("Not yet implemented")
  }

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }
}
