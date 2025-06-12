package io.allink.receipt.api.repository

import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.Sorter
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.addLogger
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 13/04/2025
 */

object TransactionUtil {
  suspend fun <T> withTransaction(block: suspend () -> T): T =
    suspendTransaction (Dispatchers.IO) {
      addLogger(StdOutSqlLogger)
      block()
    }

  suspend fun <T> withTransactionReturn(block: suspend () -> T): T {
    return suspendTransaction (Dispatchers.IO) {
      addLogger(StdOutSqlLogger)
      block()
    }
  }
}

interface ExposedRepository<
    TABLE : Table,
    T : Any,
    MODEL : BaseModel<*>> {
  val table: TABLE

  fun toModel(row: ResultRow): MODEL
  fun toRow(model: MODEL): TABLE.(UpdateBuilder<EntityID<T>>) -> Unit
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

}