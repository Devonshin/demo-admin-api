package io.allink.receipt.api.repository

import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.Sorter
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 13/04/2025
 */

object TransactionUtil {
  lateinit var db: R2dbcDatabase
  var initialized = false
  fun init(db: R2dbcDatabase) {
    if(initialized) return
    this.db = db
    initialized = true
  }

  suspend fun <T> withTransaction(isolationLevel: IsolationLevel, readonly: Boolean,  block: suspend () -> T): T =
    suspendTransaction(isolationLevel, readonly, db) {
      block()
    }

  suspend fun <T> withTransaction(block: suspend () -> T): T =
    suspendTransaction(db) {
      block()
    }

  suspend fun <T> withTransactionReturn(isolationLevel: IsolationLevel, readonly: Boolean, block: suspend () -> T): T {
    return suspendTransaction(isolationLevel, readonly, db) {
      block()
    }
  }

  suspend fun <T> withTransactionReturn(block: suspend () -> T): T {
    return suspendTransaction(db) {
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