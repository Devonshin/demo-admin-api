import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.store.StoreFilter
import io.allink.receipt.api.domain.store.StoreModel
import io.allink.receipt.api.domain.store.StoreRepository
import io.allink.receipt.api.domain.store.StoreService

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

class StoreServiceImpl(
  private val storeRepository: StoreRepository
): StoreService {
  override suspend fun findAllStore(filter: StoreFilter): PagedResult<StoreModel> {
    return storeRepository.findAll(filter)
  }

  override suspend fun findStore(id: String): StoreModel? {
    return storeRepository.find(id)
  }
}