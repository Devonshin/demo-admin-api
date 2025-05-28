package io.allink.receipt.api.repository

import io.allink.receipt.api.domain.BaseFilter
import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.domain.merchant.MerchantTagModel
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 13/04/2025
 */

interface ExposedRepository<
    TABLE : Table,
    T : Comparable<T>,
    MODEL : BaseModel<T>> {
  val table: TABLE

  suspend fun <T> query(block: suspend () -> T): T =
    newSuspendedTransaction (Dispatchers.IO) {
      addLogger(StdOutSqlLogger)
      block()
    }

  suspend fun deleteQuery(block: suspend () -> Int): Int =
    newSuspendedTransaction(Dispatchers.IO) {
      addLogger(StdOutSqlLogger)
      block()
    }

  fun toModel(row: ResultRow): MODEL
  fun toRow(model: MODEL): TABLE.(InsertStatement<EntityID<T>>) -> Unit
  fun toUpdateRow(model: MODEL): TABLE.(UpdateStatement) -> Unit

  suspend fun create(model: MODEL): MODEL

  suspend fun update(model: MODEL): Int

  suspend fun find(id: T): MODEL?

  suspend fun delete(id: T): Int

  fun columnSort(query: Query, sorters: List<Sorter>?, converter: (String) -> Expression<out Any?>?) {
    sorters?.forEach { sorter ->
      columnConvert(sorter.field)?.let { field ->
        val sortOrder =
          if (sorter.direction.equals("desc", ignoreCase = true)) {
            SortOrder.DESC
          } else {
            SortOrder.ASC
          }
        query.orderBy(
          field, sortOrder
        )
      }
    }
  }

  val columnConvert: (String?) -> Expression<out Any?>?

  fun result(
    select: Query,
    filter: BaseFilter,
    offset: Int,
    toModelList: (ResultRow) -> MODEL
  ): PagedResult<MODEL> {

    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map { toModelList(it) }

    return PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

}