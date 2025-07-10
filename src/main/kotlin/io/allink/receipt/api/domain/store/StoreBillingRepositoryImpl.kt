package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.BillingStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.r2dbc.update

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

class StoreBillingRepositoryImpl(
  override val table: StoreBillingTable,
) : StoreBillingRepository {
  override suspend fun cancelBilling(storeUid: String): Int {
    return table.update(
      where = {
        (table.storeUid eq storeUid) and
        (table.status neq BillingStatusCode.COMPLETE) and
        (table.status neq BillingStatusCode.FAIL)
      }
    ) {
      it[status] = BillingStatusCode.CANCELED
    }
  }
}