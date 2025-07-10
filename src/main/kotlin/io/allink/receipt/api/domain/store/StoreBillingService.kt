package io.allink.receipt.api.domain.store

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 10/06/2025
 */

interface StoreBillingService {
  suspend fun registBilling(billingModel: StoreBillingModel): StoreBillingModel
  suspend fun cancelBilling(storeUid: String): Int
  suspend fun updateBilling(billingModel: StoreBillingModel): StoreBillingModel
  suspend fun paymentStoreBilling(storeBillingModel: StoreBillingModel): StoreBillingModel
}