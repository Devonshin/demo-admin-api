package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.StatusCode
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.selectAll

class StoreBillingTokenRepositoryImpl(
  override val table: StoreBillingTokenTable
) : StoreBillingTokenRepository {

  override suspend fun findAllActiveByBusinessNo(businessNo: String): List<StoreBillingTokenModel>? {
    if (businessNo.isEmpty()) {
      return emptyList()
    }
    return table.selectAll()
      .where {
        table.businessNo eq businessNo
      }.andWhere {
        table.status eq StatusCode.ACTIVE
      }
      .map { toModel(it) }
      .toList()
  }

  override suspend fun findAllByBusinessNo(businessNo: String): List<StoreBillingTokenModel> {
    if (businessNo.isEmpty()) {
      return emptyList()
    }
    return table.selectAll()
      .where {
        table.businessNo eq businessNo
      }
      .map { toModel(it) }
      .toList()
  }

  override suspend fun cancelBilling(storeUid: String): Int {
    TODO("Not yet implemented")
  }

}
