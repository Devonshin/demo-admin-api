package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceRegistModel
import java.time.LocalDateTime
import java.util.UUID

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
  fun initBillingModel(
    storeUid: String,
    storeServiceSeq: Int,
    storeBilling: StoreBillingRegistModel,
    totalAmount: Int,
    now: LocalDateTime,
    userUuid: UUID
  ): StoreBillingModel

  fun calculAmount(registeredServices: List<NPointStoreServiceRegistModel>, now: LocalDateTime): Triple<Int, Int, Int>
}