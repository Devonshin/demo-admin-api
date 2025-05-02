package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.PagedResult
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

class StoreRepositoryImpl(
  override val table: StoreTable
) : StoreRepository {

}