package io.allink.receipt.api.common

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
    newSuspendedTransaction(Dispatchers.IO) {
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

  fun columnSort(query: Query, sorters: List<Sorter>?, converter: (String) -> Column<out Any?>?) =
    {
      sorters?.forEach {sorter ->
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

  val columnConvert: (String?) -> Column<out Any?>?
}