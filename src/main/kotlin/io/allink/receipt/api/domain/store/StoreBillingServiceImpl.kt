package io.allink.receipt.api.domain.store

import io.allink.receipt.api.repository.TransactionUtil

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 10/06/2025
 */

class StoreBillingServiceImpl(
  private val storeBillingRepository: StoreBillingRepository
) : StoreBillingService {

  override suspend fun registBilling(billingModel: StoreBillingModel): StoreBillingModel = TransactionUtil.withTransaction {
    storeBillingRepository.create(billingModel)
  }

  override suspend fun cancelBilling(storeUid: String): Int = TransactionUtil.withTransaction {
    storeBillingRepository.cancelBilling(storeUid)
  }

}