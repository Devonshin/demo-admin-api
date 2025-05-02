package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.store.StoreTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

class MerchantTagRepositoryImpl(
  override val table: MerchantTagTable
) : MerchantTagRepository {

}