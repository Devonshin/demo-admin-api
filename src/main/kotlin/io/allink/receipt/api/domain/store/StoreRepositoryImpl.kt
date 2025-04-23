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

  override suspend fun findAll(filter: StoreFilter): PagedResult<StoreModel> = query {
    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table.selectAll()

    filter.name?.let {name ->
      select.andWhere { table.storeName like "$name%" }
    }
    filter.franchiseCode?.let {
      select.andWhere { table.franchiseCode eq it }
    }
    filter.id?.let {
      select.andWhere { table.id eq it }
    }
    filter.businessNo?.let {
      select.andWhere { table.businessNo eq it }
    }
    filter.period.let {
      it.from.let { from ->
        select.andWhere { table.regDate greaterEq from }
      }
      it.to.let { to ->
        select.andWhere { table.regDate lessEq to }
      }
    }
    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map { toModel(it) }
    return@query PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )

  }

}