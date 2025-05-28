package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.PagedResult

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

interface StoreService {
  suspend fun findAllStore(filter: StoreFilter) : PagedResult<StoreModel>
  suspend fun findStore(id: String) : StoreModel?
  suspend fun findSearchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel>
}