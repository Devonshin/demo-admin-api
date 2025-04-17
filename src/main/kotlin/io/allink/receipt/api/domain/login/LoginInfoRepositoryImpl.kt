package io.allink.receipt.api.domain.login

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.login
 * Created: Devonshin
 * Date: 14/04/2025
 */

class LoginInfoRepositoryImpl(
  override val table: LoginInfoTable
) : LoginInfoRepository {

  override suspend fun create(model: LoginInfoModel): LoginInfoModel = query {
    val created = table.insertAndGetId(toRow(model))
    model.id = created.value
    model
  }

  override suspend fun update(model: LoginInfoModel): Int = query {
    table.update(
      where = { table.id eq model.id!! },
      body = toUpdateRow(model)
    )
  }

  override suspend fun find(id: UUID): LoginInfoModel? = query {
    table.selectAll().where {
      table.id eq id
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun delete(id: UUID): Int = deleteQuery {
    table.deleteWhere {
      table.id eq id
    }
  }


}