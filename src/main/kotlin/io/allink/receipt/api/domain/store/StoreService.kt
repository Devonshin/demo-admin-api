package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.PagedResult
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

interface StoreService {
  suspend fun findAllStore(filter: StoreFilter): PagedResult<StoreSearchModel>
  suspend fun findStore(id: String): StoreModel?
  suspend fun findSearchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel>
  suspend fun registStore(storeRegistModel: StoreRegistModel, userUuid: UUID): String
  suspend fun modifyStore(storeModifyModel: StoreModifyModel, userUuid: UUID)
  suspend fun findAllAgencyStore(filter: StoreFilter, agencyId: UUID): PagedResult<StoreSearchModel>
  suspend fun findStore(id: String, agencyId: UUID): StoreModel?
  suspend fun findAllBillingToken(businessNo: String): List<StoreBillingTokenModel>?
}